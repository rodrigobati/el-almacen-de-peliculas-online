package unrn.app;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import unrn.infra.persistence.PeliculaRepository;
import unrn.model.*;

import java.time.LocalDate;
import java.util.List;

@Component
public class DataLoader implements CommandLineRunner {

    private final PeliculaRepository repo;

    public DataLoader(PeliculaRepository repo) {
        this.repo = repo;
    }

    @Override
    public void run(String... args) throws Exception {
        // Sólo poblar si no existe una película conocida
        if (repo.existePorTitulo("Blade Runner")) {
            System.out.println("DataLoader: datos ya presentes, no se insertan ejemplos.");
            return;
        }

        try {
            var br = new Pelicula(
                    "Blade Runner",
                    Condicion.NUEVO,
                    List.of(new Director("Ridley Scott")),
                    9999.99,
                    new Formato("BLURAY"),
                    new Genero("Ciencia Ficción"),
                    "Neo-noir sci-fi classic",
                    List.of(new Actor("Harrison Ford"), new Actor("Rutger Hauer")),
                    "https://pics.filmaffinity.com/blade_runner-351607743-large.jpg",
                    LocalDate.of(1982, 6, 25),
                    5);
            var id1 = repo.guardar(br);
            System.out.println("DataLoader: insertada 'Blade Runner' id=" + id1);

            var shaw = new Pelicula(
                    "The Shawshank Redemption",
                    Condicion.NUEVO,
                    List.of(new Director("Frank Darabont")),
                    12999.0,
                    new Formato("DVD"),
                    new Genero("Drama"),
                    "Hope and friendship in prison",
                    List.of(new Actor("Tim Robbins"), new Actor("Morgan Freeman")),
                    "https://pics.filmaffinity.com/the_shawshank_redemption-576140557-large.jpg",
                    LocalDate.of(1994, 9, 23),
                    3);
            var id2 = repo.guardar(shaw);
            System.out.println("DataLoader: insertada 'The Shawshank Redemption' id=" + id2);

            var godf = new Pelicula(
                    "The Godfather",
                    Condicion.NUEVO,
                    List.of(new Director("Francis Ford Coppola")),
                    15999.0,
                    new Formato("DVD"),
                    new Genero("Crimen"),
                    "Family, power and crime",
                    List.of(new Actor("Marlon Brando"), new Actor("Al Pacino")),
                    "https://pics.filmaffinity.com/the_godfather-488102675-large.jpg",
                    LocalDate.of(1972, 3, 24),
                    4);
            var id3 = repo.guardar(godf);
            System.out.println("DataLoader: insertada 'The Godfather' id=" + id3);

            // Más ejemplos para el front-end
            var matrix = new Pelicula(
                    "The Matrix",
                    Condicion.NUEVO,
                    List.of(new Director("Lana Wachowski")),
                    11999.0,
                    new Formato("BLURAY"),
                    new Genero("Ciencia Ficción"),
                    "Reality and choice",
                    List.of(new Actor("Keanu Reeves"), new Actor("Laurence Fishburne")),
                    "https://pics.filmaffinity.com/the_matrix-155050517-large.jpg",
                    LocalDate.of(1999, 3, 31),
                    0);
            var id4 = repo.guardar(matrix);
            System.out.println("DataLoader: insertada 'The Matrix' id=" + id4);

            var dark = new Pelicula(
                    "Dark Knight",
                    Condicion.NUEVO,
                    List.of(new Director("Christopher Nolan")),
                    13999.0,
                    new Formato("BLURAY"),
                    new Genero("Accion"),
                    "Hero vs villain in Gotham",
                    List.of(new Actor("Christian Bale"), new Actor("Heath Ledger")),
                    "https://pics.filmaffinity.com/the_dark_knight-102763119-large.jpg",
                    LocalDate.of(2008, 7, 18),
                    2);
            var id5 = repo.guardar(dark);
            System.out.println("DataLoader: insertada 'Dark Knight' id=" + id5);

            var fight = new Pelicula(
                    "Fight Club",
                    Condicion.NUEVO,
                    List.of(new Director("David Fincher")),
                    8999.0,
                    new Formato("DVD"),
                    new Genero("Drama"),
                    "Identity and chaos",
                    List.of(new Actor("Brad Pitt"), new Actor("Edward Norton")),
                    "https://pics.filmaffinity.com/fight_club-320750671-large.jpg",
                    LocalDate.of(1999, 10, 15),
                    1);
            var id6 = repo.guardar(fight);
            System.out.println("DataLoader: insertada 'Fight Club' id=" + id6);

            var inception = new Pelicula(
                    "Inception",
                    Condicion.NUEVO,
                    List.of(new Director("Christopher Nolan")),
                    14999.0,
                    new Formato("BLURAY"),
                    new Genero("Ciencia Ficción"),
                    "Dream heist",
                    List.of(new Actor("Leonardo DiCaprio"), new Actor("Joseph Gordon-Levitt")),
                    "https://pics.filmaffinity.com/inception-652954101-large.jpg",
                    LocalDate.of(2010, 7, 16),
                    5);
            var id7 = repo.guardar(inception);
            System.out.println("DataLoader: insertada 'Inception' id=" + id7);

        } catch (RuntimeException ex) {
            System.out.println("DataLoader: error al insertar datos de ejemplo: " + ex.getMessage());
        }
    }
}
