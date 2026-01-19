package unrn.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Catalogo {
    static final String ERROR_PELICULAS_NULAS = "Las películas no pueden ser nulas";
    static final String ERROR_PELICULA_NULA = "No se admiten elementos nulos en el catálogo";
    static final String ERROR_CATEGORIA_NULA = "La categoría no puede ser nula";

    private final List<Pelicula> peliculas;

    public Catalogo(List<Pelicula> peliculas) {
        assertPeliculasNoNulas(peliculas);
        assertSinElementosNulos(peliculas);
        // copia defensiva
        this.peliculas = List.copyOf(new ArrayList<>(peliculas));
    }

    private void assertPeliculasNoNulas(List<Pelicula> peliculas) {
        if (peliculas == null)
            throw new RuntimeException(ERROR_PELICULAS_NULAS);
    }

    private void assertSinElementosNulos(List<Pelicula> peliculas) {
        for (Pelicula p : peliculas) {
            if (p == null)
                throw new RuntimeException(ERROR_PELICULA_NULA);
        }
    }

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

    public List<Pelicula> peliculasOrdenadasPorFechaDesc() {
        var result = peliculas.stream()
                .sorted(Comparator.comparing(Pelicula::fechaSalida, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(Pelicula::titulo))
                .collect(Collectors.toList());
        return Collections.unmodifiableList(result);
    }
}
