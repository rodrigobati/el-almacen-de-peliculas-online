package unrn.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Contrato de comando para datos de pelicula mantenido en la capa API.
 *
 * Actualmente no aparece referenciado por los controladores activos; el flujo admin
 * usa PeliculaRequest. Se conserva como artefacto de contrato/compatibilidad para
 * representar una intencion de alta o edicion desacoplada del modelo de dominio.
 */
public record PeliculaCommand(
        String titulo,
        LocalDate fechaSalida,
        BigDecimal precio,
        String condicion, // "NUEVO" | "USADO"
        String formato, // "DVD" | "BLURAY" | etc.
        String genero, // nombre o cÃ³digo
        String sinopsis,
        String imagenUrl,
        List<Long> directoresIds,
        List<Long> actoresIds) {
}
