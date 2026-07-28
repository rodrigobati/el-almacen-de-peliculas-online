package unrn.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Request de entrada para crear o editar peliculas desde la administracion.
 *
 * Contiene los datos editables del catalogo y referencias a actores/directores por
 * id. El servicio lo transforma en objetos de dominio y resuelve esas relaciones
 * antes de persistir cambios o publicar eventos.
 */
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
