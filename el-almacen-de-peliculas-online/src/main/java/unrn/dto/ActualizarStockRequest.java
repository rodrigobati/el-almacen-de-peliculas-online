package unrn.dto;

import java.math.BigDecimal;

/**
 * Request administrativo para modificar el stock disponible de una pelicula.
 *
 * Incluye el nuevo stock y la version esperada para aplicar control de concurrencia:
 * si otro usuario actualizo la pelicula antes, el servicio puede rechazar el cambio.
 */
public record ActualizarStockRequest(
        BigDecimal stockDisponible,
        Long version) {
}
