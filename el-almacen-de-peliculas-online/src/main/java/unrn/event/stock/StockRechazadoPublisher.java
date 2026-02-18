package unrn.event.stock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
public class StockRechazadoPublisher {

    static final String ROUTING_KEY = "catalogo.stock.rechazado";

    private static final Logger log = LoggerFactory.getLogger(StockRechazadoPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final TopicExchange catalogoEventsExchange;

    public StockRechazadoPublisher(RabbitTemplate rabbitTemplate,
            @Qualifier("catalogoEventsExchange") TopicExchange catalogoEventsExchange) {
        this.rabbitTemplate = rabbitTemplate;
        this.catalogoEventsExchange = catalogoEventsExchange;
    }

    public void publicarAfterCommit(StockRechazadoEvent event) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publicarAhora(event);
                }
            });
            return;
        }

        publicarAhora(event);
    }

    private void publicarAhora(StockRechazadoEvent event) {
        try {
            log.info("Publicando StockRechazado eventId={} compraId={}", event.eventId(), event.compraId());
            rabbitTemplate.convertAndSend(catalogoEventsExchange.getName(), ROUTING_KEY, event);
        } catch (RuntimeException ex) {
            log.error("No se pudo publicar StockRechazado eventId={} compraId={} mensaje={}",
                    event.eventId(), event.compraId(), ex.getMessage());
        }
    }
}
