package unrn.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas del objeto de dominio Catalogo.
 *
 * Cubren la construccion con colecciones validas, el rechazo de peliculas nulas,
 * la obtencion de categorias y el ordenamiento o filtrado de peliculas.
 */
class CatalogoTest {

    @Test
    @DisplayName("constructor listaNula lanzaExcepcion")
    void constructor_listaNula_lanzaExcepcion() {
        // Setup & ejercitaciÃ³n
        var ex = assertThrows(RuntimeException.class, () -> new Catalogo(null));
        // VerificaciÃ³n
        assertEquals(Catalogo.ERROR_PELICULAS_NULAS, ex.getMessage());
    }

    @Test
    @DisplayName("constructor listaConNulos lanzaExcepcion")
    void constructor_listaConNulos_lanzaExcepcion() {
        // Setup
        var lista = java.util.Arrays.asList((Pelicula) null);
        // EjercitaciÃ³n & VerificaciÃ³n
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
        // EjercitaciÃ³n
        var titulos = catalogo.titulosDeCategorias();
        // VerificaciÃ³n
        assertEquals(List.of("Accion", "Comedia"), titulos, "Los tÃ­tulos Ãºnicos deben estar ordenados ascendente");
    }

    @Test
    @DisplayName("filtrarPorCategoria categoriaNula lanzaExcepcion")
    void filtrarPorCategoria_categoriaNula_lanzaExcepcion() {
        // Setup
        var catalogo = new Catalogo(List.of());
        // EjercitaciÃ³n & VerificaciÃ³n
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
        // EjercitaciÃ³n
        var ordenadas = catalogo.filtrarPorCategoria(new Categoria("Drama"));
        // VerificaciÃ³n: p2 (2021 Alpha) y p3 (2021 Beta) â€” mismas fechas, desempata por
        // titulo asc -> Alpha, Beta
        assertEquals(3, ordenadas.size(), "Debe devolver las 3 pelÃ­culas de la categorÃ­a");
        assertEquals("Alpha", ordenadas.get(0).titulo(),
                "El primer tÃ­tulo debe ser Alpha (fecha mayor y desempate por tÃ­tulo)");
        assertEquals("Beta", ordenadas.get(1).titulo(), "El segundo tÃ­tulo debe ser Beta (desempate por tÃ­tulo)");
        assertEquals("Zeta", ordenadas.get(2).titulo(), "El tercer tÃ­tulo debe ser Zeta (fecha mÃ¡s vieja)");
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
        // EjercitaciÃ³n
        var ordenadas = catalogo.peliculasOrdenadasPorFechaDesc();
        // VerificaciÃ³n
        assertEquals(List.of("B", "C", "A"), ordenadas.stream().map(Pelicula::titulo).toList(),
                "El orden esperado no coincide");
    }
}
