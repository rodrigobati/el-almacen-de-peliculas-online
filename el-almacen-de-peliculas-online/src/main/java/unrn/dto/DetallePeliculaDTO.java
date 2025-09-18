package unrn.dto;
/*
En el detalle se podrán ver los siguientes datos: condición
(nuevo o usado), el título, directores, precio, formato (DVD, Blue Ray,
etc), género, resumen o sinopsis, actores, una imágen ampliada,
fecha de salida, Rating (5 estrellas) y un detalle de cada voto de cada
cliente con su comentario.
*/
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import unrn.model.Actor;
import unrn.model.Director;
import unrn.model.Pelicula;

public record DetallePeliculaDTO(
        String titulo,
        String condicion,
        List<DirectorDTO> directores,
        double precio,
        String formato,
        GeneroDTO genero,
        String sinopsis,
        List<ActorDTO> actores,
        String imagenUrl,
        LocalDate fechaSalida
) {
}

