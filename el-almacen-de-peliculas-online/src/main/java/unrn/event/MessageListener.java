package unrn.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
import unrn.model.Pelicula;
import unrn.infra.persistence.PeliculaRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.annotation.Nullable;

@Service
@Slf4j
public class MessageListener {

    static final String ERROR_PELICULA_NO_ENCONTRADA = "ERROR_PELICULA_NO_ENCONTRADA";

    private static final Logger logger = LoggerFactory.getLogger(MessageListener.class);

    @Value("${rabbitmq.event.exchange.name:peliculas.exchange}")
    private String eventExchange;

    @Value("${rabbitmq.event.catalogo.queue.name:peliculas.catalogo.queue}")
    private String queueCatalogo;

    @Value("${rabbitmq.event.catalogo.routing.key:peliculas.catalogo.key}")
    private String routingKeyCatalogo;

    private final Connection connection;
    private final ObjectMapper objectMapper;
    private final PeliculaRepository peliculaRepository;

    public MessageListener(@Nullable Connection connection, ObjectMapper objectMapper, PeliculaRepository peliculaRepository) {
        this.connection = connection;
        this.objectMapper = objectMapper;
        this.peliculaRepository = peliculaRepository;
    }

    public void processPelicula(Event<String, Pelicula> eventPelicula) {
        Long id = eventPelicula.getData().id();
        int newRating = eventPelicula.getData().rating();

        Pelicula pelicula = peliculaRepository.porId(id);
        if (pelicula == null) {
            throw new RuntimeException(ERROR_PELICULA_NO_ENCONTRADA);
        }

        pelicula.actualizarRating(newRating);
        peliculaRepository.actualizar(id, pelicula);
    }

    @PostConstruct
    public void startConsumerCatalogo() {
        if (connection == null) {
            logger.warn("RabbitMQ no disponible en el arranque: consumidor de catalogo deshabilitado.");
            return;
        }

        try {
            Channel channel = connection.createChannel();
            channel.exchangeDeclare(eventExchange, "direct", true);
            channel.queueDeclare(queueCatalogo, false, false, false, null);
            channel.queueBind(queueCatalogo, eventExchange, routingKeyCatalogo);

            logger.info("Esperando mensajes de catalogo...");

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = "";
                try {
                    message = new String(delivery.getBody(), "UTF-8");
                    logger.info("➡️ Mensaje recibido: {}", message);

                    Event<String, Pelicula> eventMovie = objectMapper.readValue(message, Event.class);
                    processPelicula(eventMovie);

                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                    logger.debug("Mensaje procesado y confirmado (ack).");

                } catch (Exception e) {
                    logger.error("❌ Error al procesar el mensaje: {}", message, e);
                    channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, false);
                }
            };

            boolean autoAck = false;
            channel.basicConsume(queueCatalogo, autoAck, deliverCallback, consumerTag -> {
                logger.warn("El consumidor fue cancelado: {}", consumerTag);
            });

        } catch (Exception e) {
            logger.error("Error al iniciar el consumidor de RabbitMQ", e);
        }
    }
}