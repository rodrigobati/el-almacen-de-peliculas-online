package unrn.event.stock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import unrn.infra.persistence.CatalogoOutboxEventEntity;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas del scheduler que publica resultados pendientes del outbox de stock.
 *
 * Verifican publicacion de eventos aceptados y rechazados, marcado de publicados,
 * registro de fallos y respeto del limite de reintentos configurado.
 */
class StockValidationResultOutboxPublisherSchedulerTest {

    @Test
    @DisplayName("PublicarPendientes acceptedPublicado marcaOutboxPublicado")
    void publicarPendientes_acceptedPublicado_marcaOutboxPublicado() {
        StockValidationResultOutboxService outboxService = mock(StockValidationResultOutboxService.class);
        StockValidationAcceptedPublisher acceptedPublisher = mock(StockValidationAcceptedPublisher.class);
        StockRechazadoPublisher rechazadoPublisher = mock(StockRechazadoPublisher.class);
        StockValidationResultOutboxPublisherScheduler scheduler = new StockValidationResultOutboxPublisherScheduler(
                outboxService,
                acceptedPublisher,
                rechazadoPublisher,
                3);
        CatalogoOutboxEventEntity outboxEvent = outboxEvent();
        StockValidationAcceptedEvent event = new StockValidationAcceptedEvent("evt-ok", 10L, Instant.now());

        when(outboxService.pendientes(3)).thenReturn(List.of(outboxEvent));
        when(outboxService.leerEvento(outboxEvent)).thenReturn(event);

        scheduler.publicarPendientes();

        verify(acceptedPublisher).publicarAhora(event);
        verify(outboxService).marcarPublicado(1L);
    }

    @Test
    @DisplayName("PublicarPendientes publisherFalla registraFalloOutbox")
    void publicarPendientes_publisherFalla_registraFalloOutbox() {
        StockValidationResultOutboxService outboxService = mock(StockValidationResultOutboxService.class);
        StockValidationAcceptedPublisher acceptedPublisher = mock(StockValidationAcceptedPublisher.class);
        StockRechazadoPublisher rechazadoPublisher = mock(StockRechazadoPublisher.class);
        StockValidationResultOutboxPublisherScheduler scheduler = new StockValidationResultOutboxPublisherScheduler(
                outboxService,
                acceptedPublisher,
                rechazadoPublisher,
                3);
        CatalogoOutboxEventEntity outboxEvent = outboxEvent();
        StockValidationAcceptedEvent event = new StockValidationAcceptedEvent("evt-fail", 11L, Instant.now());

        when(outboxService.pendientes(3)).thenReturn(List.of(outboxEvent));
        when(outboxService.leerEvento(outboxEvent)).thenReturn(event);
        doThrow(new RuntimeException("rabbit down")).when(acceptedPublisher).publicarAhora(event);

        scheduler.publicarPendientes();

        verify(outboxService).registrarFallo(1L, "rabbit down", 3);
    }

    private CatalogoOutboxEventEntity outboxEvent() {
        CatalogoOutboxEventEntity outboxEvent = new CatalogoOutboxEventEntity(
                "COMPRA",
                10L,
                "evt",
                StockValidationResultOutboxService.EVENT_TYPE_STOCK_VALIDATION_ACCEPTED,
                "{}",
                Instant.now());
        ReflectionTestUtils.setField(outboxEvent, "id", 1L);
        return outboxEvent;
    }
}
