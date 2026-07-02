package unrn.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import unrn.model.Actor;
import unrn.model.Director;
import unrn.model.Pelicula;

/**
 * DTO de pelicula para pantallas administrativas.
 *
 * A diferencia del detalle publico, incluye datos operativos como stockDisponible y
 * version, necesarios para editar inventario y evitar pisar cambios concurrentes.
 */
public record AdminPeliculaDTO(
        Long id,
        String titulo,
        String condicion,
        List<String> directores,
        double precio,
        String formato,
        String genero,
        String sinopsis,
        List<String> actores,
        String imagenUrl,
        LocalDate fechaSalida,
        int rating,
        Double ratingPromedio,
        Integer totalRatings,
        BigDecimal stockDisponible,
        long version) {

    /**
     * Construye un DTO a partir del objeto de origen recibido.
     */
    public static AdminPeliculaDTO from(Pelicula p) {
        return new AdminPeliculaDTO(
                p.id(),
                p.titulo(),
                p.condicion().valor(),
                p.directores().stream().map(Director::nombre).collect(Collectors.toUnmodifiableList()),
                p.precio(),
                p.formato().tipo(),
                p.genero().nombre(),
                p.sinopsis(),
                p.actores().stream().map(Actor::nombre).collect(Collectors.toUnmodifiableList()),
                p.imagenUrl(),
                p.fechaSalida(),
                p.rating(),
                p.ratingPromedio(),
                p.totalRatings(),
                p.stockDisponible(),
                p.version());
    }
}
