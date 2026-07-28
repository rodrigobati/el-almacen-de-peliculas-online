package unrn.event.stock;

import java.util.List;
import java.util.UUID;

/**
 * Evento de salida que informa a ventas que catalogo rechazo una reserva de stock.
 *
 * Incluye el motivo general y detalles por pelicula para que la compra pueda quedar
 * observada, cancelada o informada al cliente con una causa concreta.
 */
public record StockRechazadoEvent(
        String eventId,
        Long compraId,
        String motivo,
        List<DetalleStockRechazado> detalles) {

    /**
     * Inicializa una instancia de StockRechazadoEvent con los datos necesarios.
     */
    public StockRechazadoEvent(Long compraId, String motivo, List<DetalleStockRechazado> detalles) {
        this(UUID.randomUUID().toString(), compraId, motivo, detalles == null ? List.of() : List.copyOf(detalles));
    }

    /**
     * Detalle por item rechazado dentro de un evento de stock rechazado.
     *
     * Permite informar que pelicula fallo, que cantidad se solicito y cuanto stock
     * estaba disponible cuando corresponde.
     */
    public record DetalleStockRechazado(Long peliculaId, int solicitado, String disponible) {
    }
}
