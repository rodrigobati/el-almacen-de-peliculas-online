package unrn.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
import org.springframework.data.crossstore.ChangeSetPersister;
import unrn.model.Pelicula;
import unrn.infra.persistence.PeliculaRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MessageListener {

    private static final Logger logger = LoggerFactory.getLogger(MessageListener.class);

    // Event
    @Value("${rabbitmq.event.exchange.name}")
    private String eventExchange;

    // KeyCloak
    @Value("${rabbitmq.event.catalogo.queue.name}")
    private String queueCatalogo;

    @Value("${rabbitmq.event.catalogo.routing.key}")
    private String routingKeyCatalogo;

    private final Connection connection;

    private final ObjectMapper objectMapper;

    public MessageListener(Connection connection, ObjectMapper objectMapper) {
        this.connection = connection;
        this.objectMapper = new ObjectMapper();
    }

    public void processPelicula(Event<String, Pelicula> eventPelicula) {
        Long id = eventPelicula.getData().getId();
        int newRating = eventPelicula.getData().getRating();

        Pelicula pelicula = PeliculaRepository.porId(id);
        pelicula.actualizarRating(newRating);
        PeliculaRepository.actualizar(id ,pelicula);
    }


    @PostConstruct
    public void startConsumerCatalogo() {
        try {
            Channel channel = connection.createChannel();

            // Declarar el exchange (tipo puede ser "direct", "topic", "fanout", o "headers")
            //
            channel.exchangeDeclare(eventExchange, "direct", true);

            // Declarar la cola
            // Durable: false, Exclusive: false, Auto-delete: false
            channel.queueDeclare(queueCatalogo, false, false, false, null);

            // Enlazar la cola con el exchange usando una routing key
            channel.queueBind(queueCatalogo, eventExchange, routingKeyCatalogo);

            System.out.println("Esperando mensajes...");

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = "";
                try {
                    message = new String(delivery.getBody(), "UTF-8");
                    logger.info("➡️ Mensaje recibido: {}", message);

                    // Lógica de negocio: deserializar y procesar
                    Event<String, Pelicula> eventMovie = objectMapper.readValue(message, Event.class);
                    processPelicula(eventMovie); // <-- Llama a tu lógica de negocio aquí

                    // 1. CONFIRMACIÓN MANUAL: Si todo fue bien, confirma el mensaje.
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                    logger.debug("Mensaje procesado y confirmado (ack).");

                } catch (Exception e) {
                    // 2. RECHAZO: Si algo falló, rechaza el mensaje.
                    logger.error("❌ Error al procesar el mensaje: {}", message, e);
                    // El tercer parámetro 'requeue' decide si el mensaje vuelve a la cola.
                    // Ponerlo en 'false' es usualmente más seguro para evitar bucles infinitos
                    // con mensajes que siempre fallan. Idealmente, se enviaría a una Dead Letter Queue (DLQ).
                    channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, false);
                }
            };

            // Iniciar consumo con autoAck = false
            boolean autoAck = false;
            channel.basicConsume(queueCatalogo, autoAck, deliverCallback, consumerTag -> {
                logger.warn("El consumidor fue cancelado: {}", consumerTag);
            });

        } catch (Exception e) {
            // Este catch ahora es para errores de configuración inicial (ej. no se puede conectar a RabbitMQ)
            logger.error("🔥 Error crítico al configurar el consumidor de RabbitMQ", e);
        }
    }
}
