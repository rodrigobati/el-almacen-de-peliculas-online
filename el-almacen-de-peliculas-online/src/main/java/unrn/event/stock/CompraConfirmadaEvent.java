package unrn.event.stock;

import java.time.Instant;
import java.util.List;

/**
 * Evento de compra confirmada recibido desde la vertical ventas.
 *
 * Representa una compra ya aceptada por ventas y contiene los items que catalogo
 * debe validar contra stock. Se usa en el flujo legado y como forma interna comun
 * para procesar solicitudes de validacion de stock.
 */
public record CompraConfirmadaEvent(
        String eventId,
        Long compraId,
        String clienteId,
        Instant fechaHora,
        List<ItemCompraConfirmada> items) {

    /**
     * Item de una compra que requiere stock de una pelicula concreta.
     *
     * Guarda el identificador de pelicula y la cantidad solicitada para que el
     * servicio pueda validar existencia, actividad y disponibilidad.
     */
    public record ItemCompraConfirmada(Long peliculaId, int cantidad) {
    }
}
