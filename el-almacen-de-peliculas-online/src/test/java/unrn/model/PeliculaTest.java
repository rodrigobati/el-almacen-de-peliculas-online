package unrn.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import unrn.dto.DetallePeliculaDTO;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PeliculaTest {
    @Test
    @DisplayName("El detalle completo de la película contiene todos los datos relevantes")
    void detalleCompleto_devuelveDatosEsperados() {
        // Setup: crear modelo con Value Objects
        var pelicula = new Pelicula(
                "Matrix",
                new Condicion("nuevo"),
                List.of(new Director("Lana Wachowski"), new Director("Lilly Wachowski")),
                1500.0,
                new Formato("Blu-ray"),
                new Genero("Ciencia Ficción"),
                "Un hacker descubre la verdad sobre su realidad.",
                List.of(new Actor("Keanu Reeves"), new Actor("Carrie-Anne Moss")),
                "https://ejemplo.com/matrix.jpg",
                LocalDate.of(1999, 3, 31),
                5);

        // Ejercitación: transformar a DTO
        var detalle = DetallePeliculaDTO.from(pelicula);

        // Verificación: todos los campos primitivos/strings están presentes
        assertEquals("Matrix", detalle.titulo(), "El título esperado debe coincidir");
        assertEquals("nuevo", detalle.condicion(), "La condición debe ser 'nuevo'");
        assertEquals(List.of("Lana Wachowski", "Lilly Wachowski"), detalle.directores(), "Los directores no coinciden");
        assertEquals(1500.0, detalle.precio(), "El precio no coincide");
        assertEquals("Blu-ray", detalle.formato(), "El formato no coincide");
        assertEquals("Ciencia Ficción", detalle.genero(), "El género no coincide");
        assertEquals("Un hacker descubre la verdad sobre su realidad.", detalle.sinopsis(), "La sinopsis no coincide");
        assertEquals(List.of("Keanu Reeves", "Carrie-Anne Moss"), detalle.actores(), "Los actores no coinciden");
        assertEquals("https://ejemplo.com/matrix.jpg", detalle.imagenUrl(), "La URL de la imagen no coincide");
        assertEquals(LocalDate.of(1999, 3, 31), detalle.fechaSalida(), "La fecha de salida no coincide");
    }

    @Test
    @DisplayName("No permite crear película con título vacío")
    void constructor_tituloVacio_lanzaExcepcion() {
        // Setup: parámetros válidos excepto el título
        var ex = assertThrows(RuntimeException.class, () -> new Pelicula(
                "",
                new Condicion("nuevo"),
                List.of(new Director("Director")),
                1000.0,
                new Formato("DVD"),
                new Genero("Acción"),
                "Sinopsis",
                List.of(new Actor("Actor")),
                "url",
                LocalDate.now(),
                5));
        assertEquals(Pelicula.ERROR_TITULO, ex.getMessage());
    }

    @Test
    @DisplayName("No permite crear película con precio negativo")
    void constructor_precioNegativo_lanzaExcepcion() {
        var ex = assertThrows(RuntimeException.class, () -> new Pelicula(
                "Titulo",
                new Condicion("nuevo"),
                List.of(new Director("Director")),
                -10.0,
                new Formato("DVD"),
                new Genero("Acción"),
                "Sinopsis",
                List.of(new Actor("Actor")),
                "url",
                LocalDate.now(),
                5));
        assertEquals(Pelicula.ERROR_PRECIO, ex.getMessage());
    }

    @Test
    @DisplayName("No permite crear Condicion inválida")
    void constructor_condicionInvalida_lanzaExcepcion() {
        var ex = assertThrows(RuntimeException.class, () -> new Condicion("reparado"));
        assertEquals(Condicion.ERROR_CONDICION, ex.getMessage());
    }
}
