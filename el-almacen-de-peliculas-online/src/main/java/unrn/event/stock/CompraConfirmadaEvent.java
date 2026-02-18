package unrn.event.stock;

import java.time.Instant;
import java.util.List;

public record CompraConfirmadaEvent(
        String eventId,
        Long compraId,
        String clienteId,
        Instant fechaHora,
        List<ItemCompraConfirmada> items) {

    public record ItemCompraConfirmada(Long peliculaId, int cantidad) {
    }
}
