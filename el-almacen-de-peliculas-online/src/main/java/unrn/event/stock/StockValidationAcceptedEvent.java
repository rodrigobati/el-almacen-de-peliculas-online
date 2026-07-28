package unrn.event.stock;

import java.time.Instant;

/**
 * Evento de salida que confirma a ventas que el stock fue validado y reservado.
 *
 * Se genera cuando todos los items de una compra pasan las reglas de existencia,
 * actividad, cantidad positiva y disponibilidad de stock.
 */
public record StockValidationAcceptedEvent(
        String eventId,
        Long compraId,
        Instant occurredAt) {
}
