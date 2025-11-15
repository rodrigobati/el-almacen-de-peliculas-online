package unrn.dto;

import java.time.LocalDate;
import java.util.List;

public record PeliculaRequest(
                String titulo,
                String condicion, // para crear Condicion
                List<Long> directoresIds,
                double precio, // o BigDecimal, pero tu Pelicula usa double
                String formato, // para crear Formato
                String genero, // para crear Genero
                String sinopsis,
                List<Long> actoresIds,
                String imagenUrl,
                LocalDate fechaSalida,
                int rating) {
}
