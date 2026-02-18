package unrn.event.stock;

import java.util.List;
import java.util.UUID;

public record StockRechazadoEvent(
        String eventId,
        Long compraId,
        String motivo,
        List<DetalleStockRechazado> detalles) {

    public StockRechazadoEvent(Long compraId, String motivo, List<DetalleStockRechazado> detalles) {
        this(UUID.randomUUID().toString(), compraId, motivo, detalles == null ? List.of() : List.copyOf(detalles));
    }

    public record DetalleStockRechazado(Long peliculaId, int solicitado, String disponible) {
    }
}
