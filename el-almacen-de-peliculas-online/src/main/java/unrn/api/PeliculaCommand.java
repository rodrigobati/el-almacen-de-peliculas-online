package unrn.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PeliculaCommand(
        String titulo,
        LocalDate fechaSalida,
        BigDecimal precio,
        String condicion, // "NUEVO" | "USADO"
        String formato, // "DVD" | "BLURAY" | etc.
        String genero, // nombre o código
        String sinopsis,
        String imagenUrl,
        List<Long> directoresIds,
        List<Long> actoresIds) {
}
