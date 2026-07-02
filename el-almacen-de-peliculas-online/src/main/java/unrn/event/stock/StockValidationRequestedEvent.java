package unrn.event.stock;

import java.time.Instant;
import java.util.List;

/**
 * Evento de entrada que solicita validar stock antes de confirmar una compra.
 *
 * Es el contrato del flujo nuevo entre ventas y catalogo: ventas pide validar items
 * y catalogo responde luego con un evento aceptado o rechazado.
 */
public record StockValidationRequestedEvent(
        String eventId,
        Long compraId,
        List<Item> items,
        Instant occurredAt) {

    /**
     * Item solicitado en una validacion de stock.
     *
     * Identifica la pelicula y la cantidad que ventas quiere reservar antes de dar
     * por confirmada la compra.
     */
    public record Item(Long peliculaId, int cantidad) {
    }
}
