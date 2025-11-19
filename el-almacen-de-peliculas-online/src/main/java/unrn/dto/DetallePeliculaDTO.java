package unrn.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import unrn.model.Actor;
import unrn.model.Director;
import unrn.model.Pelicula;

public record DetallePeliculaDTO(
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
                Integer totalRatings) {

        public static DetallePeliculaDTO from(Pelicula p) {
                return new DetallePeliculaDTO(
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
                                p.totalRatings());
        }
}
