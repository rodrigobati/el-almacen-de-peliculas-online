package unrn.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CatalogoTest {

    @Test
    @DisplayName("constructor listaNula lanzaExcepcion")
    void constructor_listaNula_lanzaExcepcion() {
        // Setup & ejercitación
        var ex = assertThrows(RuntimeException.class, () -> new Catalogo(null));
        // Verificación
        assertEquals(Catalogo.ERROR_PELICULAS_NULAS, ex.getMessage());
    }

    @Test
    @DisplayName("constructor listaConNulos lanzaExcepcion")
    void constructor_listaConNulos_lanzaExcepcion() {
        // Setup
        var lista = java.util.Arrays.asList((Pelicula) null);
        // Ejercitación & Verificación
        var ex = assertThrows(RuntimeException.class, () -> new Catalogo(lista));
        assertEquals(Catalogo.ERROR_PELICULA_NULA, ex.getMessage());
    }

    @Test
    @DisplayName("titulosDeCategorias devuelveUnicosOrdenadosAsc")
    void titulosDeCategorias_devuelveUnicosOrdenadosAsc() {
        // Setup
        var p1 = new Pelicula("A", new Condicion("nuevo"), List.of(new Director("D")), 1.0, new Formato("F"),
                new Genero("Comedia"), "s", List.of(new Actor("a")), "i", LocalDate.of(2020, 1, 1), 0);
        var p2 = new Pelicula("B", new Condicion("nuevo"), List.of(new Director("D")), 1.0, new Formato("F"),
                new Genero("Accion"), "s", List.of(new Actor("a")), "i", LocalDate.of(2021, 1, 1), 0);
        var p3 = new Pelicula("C", new Condicion("nuevo"), List.of(new Director("D")), 1.0, new Formato("F"),
                new Genero("Comedia"), "s", List.of(new Actor("a")), "i", LocalDate.of(2019, 1, 1), 0);
        var catalogo = new Catalogo(List.of(p1, p2, p3));
        // Ejercitación
        var titulos = catalogo.titulosDeCategorias();
        // Verificación
        assertEquals(List.of("Accion", "Comedia"), titulos, "Los títulos únicos deben estar ordenados ascendente");
    }

    @Test
    @DisplayName("filtrarPorCategoria categoriaNula lanzaExcepcion")
    void filtrarPorCategoria_categoriaNula_lanzaExcepcion() {
        // Setup
        var catalogo = new Catalogo(List.of());
        // Ejercitación & Verificación
        var ex = assertThrows(RuntimeException.class, () -> catalogo.filtrarPorCategoria(null));
        assertEquals(Catalogo.ERROR_CATEGORIA_NULA, ex.getMessage());
    }

    @Test
    @DisplayName("filtrarPorCategoria devuelvePeliculasOrdenadasPorFechaDesc")
    void filtrarPorCategoria_devuelvePeliculasOrdenadasPorFechaDesc() {
        // Setup
        var genero = new Genero("Drama");
        var p1 = new Pelicula("Zeta", new Condicion("nuevo"), List.of(new Director("D")), 1.0, new Formato("F"), genero,
                "s", List.of(new Actor("a")), "i", LocalDate.of(2020, 1, 1), 0);
        var p2 = new Pelicula("Alpha", new Condicion("nuevo"), List.of(new Director("D")), 1.0, new Formato("F"),
                genero, "s", List.of(new Actor("a")), "i", LocalDate.of(2021, 1, 1), 0);
        var p3 = new Pelicula("Beta", new Condicion("nuevo"), List.of(new Director("D")), 1.0, new Formato("F"), genero,
                "s", List.of(new Actor("a")), "i", LocalDate.of(2021, 1, 1), 0);
        var catalogo = new Catalogo(List.of(p1, p2, p3));
        // Ejercitación
        var ordenadas = catalogo.filtrarPorCategoria(new Categoria("Drama"));
        // Verificación: p2 (2021 Alpha) y p3 (2021 Beta) — mismas fechas, desempata por
        // titulo asc -> Alpha, Beta
        assertEquals(3, ordenadas.size(), "Debe devolver las 3 películas de la categoría");
        assertEquals("Alpha", ordenadas.get(0).titulo(),
                "El primer título debe ser Alpha (fecha mayor y desempate por título)");
        assertEquals("Beta", ordenadas.get(1).titulo(), "El segundo título debe ser Beta (desempate por título)");
        assertEquals("Zeta", ordenadas.get(2).titulo(), "El tercer título debe ser Zeta (fecha más vieja)");
    }

    @Test
    @DisplayName("peliculasOrdenadasPorFechaDesc devuelveTodoOrdenado")
    void peliculasOrdenadasPorFechaDesc_devuelveTodoOrdenado() {
        // Setup
        var p1 = new Pelicula("A", new Condicion("nuevo"), List.of(new Director("D")), 1.0, new Formato("F"),
                new Genero("G"), "s", List.of(new Actor("a")), "i", LocalDate.of(2018, 1, 1), 0);
        var p2 = new Pelicula("B", new Condicion("nuevo"), List.of(new Director("D")), 1.0, new Formato("F"),
                new Genero("G"), "s", List.of(new Actor("a")), "i", LocalDate.of(2020, 1, 1), 0);
        var p3 = new Pelicula("C", new Condicion("nuevo"), List.of(new Director("D")), 1.0, new Formato("F"),
                new Genero("G"), "s", List.of(new Actor("a")), "i", LocalDate.of(2019, 1, 1), 0);
        var catalogo = new Catalogo(List.of(p1, p2, p3));
        // Ejercitación
        var ordenadas = catalogo.peliculasOrdenadasPorFechaDesc();
        // Verificación
        assertEquals(List.of("B", "C", "A"), ordenadas.stream().map(Pelicula::titulo).toList(),
                "El orden esperado no coincide");
    }
}
