package unrn.event.stock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import unrn.infra.persistence.CatalogoOutboxEventEntity;

/**
 * Tarea programada que publica resultados pendientes del outbox de stock.
 *
 * Busca eventos aceptados o rechazados todavia no publicados, los envia por el
 * publisher correspondiente y actualiza su estado. Si falla, registra el error y
 * deja controlado el numero de reintentos.
 */
@Component
@ConditionalOnProperty(name = "catalogo.outbox.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class StockValidationResultOutboxPublisherScheduler {

    private static final Logger log = LoggerFactory.getLogger(StockValidationResultOutboxPublisherScheduler.class);

    private final StockValidationResultOutboxService outboxService;
    private final StockValidationAcceptedPublisher acceptedPublisher;
    private final StockRechazadoPublisher rechazadoPublisher;
    private final int maxAttempts;

    /**
     * Inicializa una instancia de StockValidationResultOutboxPublisherScheduler con los datos necesarios.
     */
    public StockValidationResultOutboxPublisherScheduler(StockValidationResultOutboxService outboxService,
            StockValidationAcceptedPublisher acceptedPublisher,
            StockRechazadoPublisher rechazadoPublisher,
            @Value("${catalogo.outbox.max-attempts:10}") int maxAttempts) {
        this.outboxService = outboxService;
        this.acceptedPublisher = acceptedPublisher;
        this.rechazadoPublisher = rechazadoPublisher;
        this.maxAttempts = maxAttempts;
    }

    /**
     * Publica todos los eventos outbox pendientes encontrados por el scheduler.
     */
    @Scheduled(fixedDelayString = "${catalogo.outbox.scheduler.delay-ms:3000}")
    public void publicarPendientes() {
        for (CatalogoOutboxEventEntity outboxEvent : outboxService.pendientes(maxAttempts)) {
            publicar(outboxEvent);
        }
    }

    /**
     * Publica un evento outbox y actualiza su estado segun el resultado.
     */
    private void publicar(CatalogoOutboxEventEntity outboxEvent) {
        try {
            Object event = outboxService.leerEvento(outboxEvent);
            if (event instanceof StockValidationAcceptedEvent acceptedEvent) {
                acceptedPublisher.publicarAhora(acceptedEvent);
            } else if (event instanceof StockRechazadoEvent rechazadoEvent) {
                rechazadoPublisher.publicarAhora(rechazadoEvent);
            } else {
                throw new RuntimeException(StockValidationResultOutboxService.ERROR_EVENT_TYPE_NO_SOPORTADO);
            }
            outboxService.marcarPublicado(outboxEvent.id());
        } catch (RuntimeException ex) {
            outboxService.registrarFallo(outboxEvent.id(), ex.getMessage(), maxAttempts);
            log.warn("No se pudo publicar evento outbox catalogo id={} error={}",
                    outboxEvent.id(), ex.getMessage());
        }
    }
}
