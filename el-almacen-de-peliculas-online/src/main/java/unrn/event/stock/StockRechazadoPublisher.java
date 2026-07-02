package unrn.event.stock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Publicador RabbitMQ para eventos de stock rechazado.
 *
 * Envia el rechazo hacia ventas usando la routing key configurada y prioriza la
 * publicacion despues del commit para no notificar rechazos de transacciones que no
 * se confirmaron.
 */
@Component
public class StockRechazadoPublisher {

    static final String ROUTING_KEY = "catalogo.stock.rechazado";

    private static final Logger log = LoggerFactory.getLogger(StockRechazadoPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final TopicExchange catalogoEventsExchange;

    /**
     * Inicializa una instancia de StockRechazadoPublisher con los datos necesarios.
     */
    public StockRechazadoPublisher(RabbitTemplate rabbitTemplate,
            @Qualifier("catalogoEventsExchange") TopicExchange catalogoEventsExchange) {
        this.rabbitTemplate = rabbitTemplate;
        this.catalogoEventsExchange = catalogoEventsExchange;
    }

    /**
     * Agenda la publicacion del evento para despues del commit transaccional.
     */
    public void publicarAfterCommit(StockRechazadoEvent event) {
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
    public void publicarAhora(StockRechazadoEvent event) {
        try {
            log.info("Publicando StockRechazado eventId={} compraId={}", event.eventId(), event.compraId());
            rabbitTemplate.convertAndSend(catalogoEventsExchange.getName(), ROUTING_KEY, event);
        } catch (RuntimeException ex) {
            log.error("No se pudo publicar StockRechazado eventId={} compraId={} mensaje={}",
                    event.eventId(), event.compraId(), ex.getMessage());
            throw ex;
        }
    }
}
