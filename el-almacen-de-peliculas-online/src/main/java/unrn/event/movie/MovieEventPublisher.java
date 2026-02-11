package unrn.event.movie;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
public class MovieEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(MovieEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final TopicExchange eventExchange;

    public MovieEventPublisher(RabbitTemplate rabbitTemplate,
            @Qualifier("exchangeVideoCloub00") TopicExchange eventExchange) {
        this.rabbitTemplate = rabbitTemplate;
        this.eventExchange = eventExchange;
    }

    public void publishAfterCommit(MovieEventEnvelope envelope) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publishNow(envelope);
                }
            });
            return;
        }

        publishNow(envelope);
    }

    public void publishNow(MovieEventEnvelope envelope) {
        log.info("Publicando evento de pelicula {} con routing key {}", envelope.eventId(), envelope.eventType());
        rabbitTemplate.convertAndSend(eventExchange.getName(), envelope.eventType(), envelope);
    }
}
