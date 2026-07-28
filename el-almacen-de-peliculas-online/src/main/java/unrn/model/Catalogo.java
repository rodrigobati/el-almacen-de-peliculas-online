package unrn.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Agrupa una coleccion de peliculas y expresa consultas propias del catalogo.
 *
 * No persiste datos ni conoce HTTP: trabaja sobre peliculas de dominio ya cargadas
 * para listar categorias, filtrar por categoria y ordenar por fecha. Su copia
 * defensiva protege la lista recibida para que las consultas sean consistentes.
 */
public class Catalogo {
    static final String ERROR_PELICULAS_NULAS = "Las pelÃ­culas no pueden ser nulas";
    static final String ERROR_PELICULA_NULA = "No se admiten elementos nulos en el catÃ¡logo";
    static final String ERROR_CATEGORIA_NULA = "La categorÃ­a no puede ser nula";

    private final List<Pelicula> peliculas;

    /**
     * Inicializa una instancia de Catalogo con los datos necesarios.
     */
    public Catalogo(List<Pelicula> peliculas) {
        assertPeliculasNoNulas(peliculas);
        assertSinElementosNulos(peliculas);
        // copia defensiva
        this.peliculas = List.copyOf(new ArrayList<>(peliculas));
    }

    /**
     * Exige que el catalogo se construya con una lista de peliculas.
     */
    private void assertPeliculasNoNulas(List<Pelicula> peliculas) {
        if (peliculas == null)
            throw new RuntimeException(ERROR_PELICULAS_NULAS);
    }

    /**
     * Impide que el catalogo contenga peliculas nulas.
     */
    private void assertSinElementosNulos(List<Pelicula> peliculas) {
        for (Pelicula p : peliculas) {
            if (p == null)
                throw new RuntimeException(ERROR_PELICULA_NULA);
        }
    }

    /**
     * Devuelve los titulos de categorias unicos y ordenados.
     */
    public List<String> titulosDeCategorias() {
        var titulos = peliculas.stream()
                .map(p -> p.genero() != null ? p.genero().nombre() : null)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::valueOf)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        return Collections.unmodifiableList(titulos);
    }

    /**
     * Filtra peliculas por categoria y las ordena por fecha descendente.
     */
    public List<Pelicula> filtrarPorCategoria(Categoria categoria) {
        if (categoria == null)
            throw new RuntimeException(ERROR_CATEGORIA_NULA);
        var result = peliculas.stream()
                .filter(p -> p.genero() != null && categoria.titulo().equalsIgnoreCase(p.genero().nombre()))
                .sorted(Comparator.comparing(Pelicula::fechaSalida, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(Pelicula::titulo))
                .collect(Collectors.toList());
        return Collections.unmodifiableList(result);
    }

    /**
     * Devuelve todas las peliculas ordenadas por fecha descendente.
     */
    public List<Pelicula> peliculasOrdenadasPorFechaDesc() {
        var result = peliculas.stream()
                .sorted(Comparator.comparing(Pelicula::fechaSalida, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(Pelicula::titulo))
                .collect(Collectors.toList());
        return Collections.unmodifiableList(result);
    }
}
