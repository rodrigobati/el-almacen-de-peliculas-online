package unrn.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PeliculaAdminTest {

    @Test
    @DisplayName("crearPelicula tituloVacio lanzaExcepcion")
    void crearPelicula_tituloVacio_lanzaExcepcion() {
        // Setup: Preparar el escenario
        String titulo = " ";

        // Ejercitación: Ejecutar la acción a probar
        var ex = assertThrows(RuntimeException.class, () -> new Pelicula(
                titulo,
                new Condicion("nuevo"),
                List.of(new Director("Director")),
                1000.0,
                new Formato("DVD"),
                new Genero("Accion"),
                "Sinopsis",
                List.of(new Actor("Actor")),
                "url",
                LocalDate.now(),
                5));

        // Verificación: Verificar el resultado esperado
        assertEquals(Pelicula.ERROR_TITULO, ex.getMessage(), "El mensaje de error no coincide");
    }

    @Test
    @DisplayName("crearPelicula precioInvalido lanzaExcepcion")
    void crearPelicula_precioInvalido_lanzaExcepcion() {
        // Setup: Preparar el escenario
        double precio = 0.0;

        // Ejercitación: Ejecutar la acción a probar
        var ex = assertThrows(RuntimeException.class, () -> new Pelicula(
                "Titulo",
                new Condicion("nuevo"),
                List.of(new Director("Director")),
                precio,
                new Formato("DVD"),
                new Genero("Accion"),
                "Sinopsis",
                List.of(new Actor("Actor")),
                "url",
                LocalDate.now(),
                5));

        // Verificación: Verificar el resultado esperado
        assertEquals(Pelicula.ERROR_PRECIO, ex.getMessage(), "El mensaje de error no coincide");
    }

    @Test
    @DisplayName("retirarPelicula quedaInactiva")
    void retirarPelicula_quedaInactiva() {
        // Setup: Preparar el escenario
        var pelicula = new Pelicula(
                "Titulo",
                new Condicion("nuevo"),
                List.of(new Director("Director")),
                1200.0,
                new Formato("DVD"),
                new Genero("Accion"),
                "Sinopsis",
                List.of(new Actor("Actor")),
                "url",
                LocalDate.now(),
                5);

        // Ejercitación: Ejecutar la acción a probar
        pelicula.retirar();

        // Verificación: Verificar el resultado esperado
        assertFalse(pelicula.activa(), "La película debería quedar inactiva");
    }
}
