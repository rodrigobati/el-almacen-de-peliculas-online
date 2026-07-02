package unrn.event.stock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Publicador RabbitMQ para resultados aceptados de validacion de stock.
 *
 * Notifica a ventas que catalogo pudo reservar el stock solicitado. Igual que otros
 * publishers transaccionales, espera el commit cuando existe una transaccion activa.
 */
@Component
public class StockValidationAcceptedPublisher {

    private static final Logger log = LoggerFactory.getLogger(StockValidationAcceptedPublisher.class);

    @Value("${rabbitmq.catalogo.stock.validation.accepted.routing-key:catalogo.stock.validation.accepted}")
    private String routingKey;

    private final RabbitTemplate rabbitTemplate;
    private final TopicExchange catalogoEventsExchange;

    /**
     * Inicializa una instancia de StockValidationAcceptedPublisher con los datos necesarios.
     */
    public StockValidationAcceptedPublisher(RabbitTemplate rabbitTemplate,
            @Qualifier("catalogoEventsExchange") TopicExchange catalogoEventsExchange) {
        this.rabbitTemplate = rabbitTemplate;
        this.catalogoEventsExchange = catalogoEventsExchange;
    }

    /**
     * Agenda la publicacion del evento para despues del commit transaccional.
     */
    public void publicarAfterCommit(StockValidationAcceptedEvent event) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                /**
                 * Ejecuta la accion diferida una vez confirmado el commit de la transaccion.
                 */
                @Override
                public void afterCommit() {
                    publicarAhora(event);
                }
            });
            return;
        }

        publicarAhora(event);
    }

    /**
     * Publica inmediatamente el evento en RabbitMQ.
     */
    public void publicarAhora(StockValidationAcceptedEvent event) {
        try {
            log.info("Publicando StockValidationAccepted eventId={} compraId={}", event.eventId(), event.compraId());
            rabbitTemplate.convertAndSend(catalogoEventsExchange.getName(), routingKey, event);
        } catch (RuntimeException ex) {
            log.error("No se pudo publicar StockValidationAccepted eventId={} compraId={} mensaje={}",
                    event.eventId(), event.compraId(), ex.getMessage());
            throw ex;
        }
    }
}
