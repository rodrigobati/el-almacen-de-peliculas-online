package unrn.app;

import unrn.model.*;
import unrn.infra.persistence.PeliculaRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * Utilidad de consola para probar persistencia de peliculas manualmente.
 *
 * Permite crear datos y verificar el mapeo contra la base durante desarrollo,
 * separada del arranque Spring Boot principal y de los endpoints HTTP.
 */
public class DemoPersist {
    /**
     * Punto de entrada para ejecutar esta aplicacion.
     */
    public static void main(String[] args) {
        PeliculaRepository repo = new PeliculaRepository();

        Pelicula peli = new Pelicula(
                "Blade Runner",
                new Condicion("nuevo"),
                List.of(new Director("Ridley Scott")),
                9999.99,
                new Formato("BLURAY"),
                new Genero("Ciencia FicciÃ³n"),
                "Neo-noir sci-fi classic",
                List.of(new Actor("Harrison Ford"), new Actor("Rutger Hauer")),
                "https://example.com/br.jpg",
                LocalDate.of(1982, 6, 25),
                5);

        Pelicula guardada = repo.guardar(peli);
        System.out.println("Guardada Pelicula con id=" + guardada.id());

        Pelicula recuperada = repo.porId(guardada.id());
        System.out.println("Recuperada: " + recuperada.titulo() + " (" + recuperada.genero() + ")");
    }
}
