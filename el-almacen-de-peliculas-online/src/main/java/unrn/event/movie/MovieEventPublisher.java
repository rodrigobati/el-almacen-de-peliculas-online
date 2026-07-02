package unrn.event.movie;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Publicador RabbitMQ de eventos de peliculas del catalogo.
 *
 * Envia los cambios despues del commit transaccional para evitar informar eventos
 * de operaciones que luego fallen. Tambien permite publicar inmediatamente cuando
 * no hay una transaccion activa.
 */
@Component
public class MovieEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(MovieEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final TopicExchange eventExchange;

    /**
     * Inicializa una instancia de MovieEventPublisher con los datos necesarios.
     */
    public MovieEventPublisher(RabbitTemplate rabbitTemplate,
            @Qualifier("exchangeVideoCloub00") TopicExchange eventExchange) {
        this.rabbitTemplate = rabbitTemplate;
        this.eventExchange = eventExchange;
    }

    /**
     * Agenda la publicacion del evento de pelicula para despues del commit.
     */
    public void publishAfterCommit(MovieEventEnvelope envelope) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                /**
                 * Ejecuta la accion diferida una vez confirmado el commit de la transaccion.
                 */
                @Override
                public void afterCommit() {
                    publishNow(envelope);
                }
            });
            return;
        }

        publishNow(envelope);
    }

    /**
     * Publica inmediatamente el evento de pelicula en RabbitMQ.
     */
    public void publishNow(MovieEventEnvelope envelope) {
        log.info("Publicando evento de pelicula {} con routing key {}", envelope.eventId(), envelope.eventType());
        rabbitTemplate.convertAndSend(eventExchange.getName(), envelope.eventType(), envelope);
    }
}
