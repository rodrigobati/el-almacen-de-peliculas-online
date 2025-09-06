package unrn.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PeliculaTest {
    @Test
    @DisplayName("El detalle completo de la película contiene todos los datos relevantes")
    void detalleCompleto_devuelveDatosEsperados() {
        var pelicula = new Pelicula(
            "Matrix",
            "nuevo",
            List.of("Lana Wachowski", "Lilly Wachowski"),
            1500.0,
            "Blu-ray",
            "Ciencia Ficción",
            "Un hacker descubre la verdad sobre su realidad.",
            List.of("Keanu Reeves", "Carrie-Anne Moss"),
            "https://ejemplo.com/matrix.jpg",
            LocalDate.of(1999, 3, 31)
        );
        var detalle = pelicula.detalleCompleto();
        assertEquals("Matrix", detalle.titulo);
        assertEquals("nuevo", detalle.condicion);
        assertEquals(List.of("Lana Wachowski", "Lilly Wachowski"), detalle.directores);
        assertEquals(1500.0, detalle.precio);
        assertEquals("Blu-ray", detalle.formato);
        assertEquals("Ciencia Ficción", detalle.genero);
        assertEquals("Un hacker descubre la verdad sobre su realidad.", detalle.sinopsis);
        assertEquals(List.of("Keanu Reeves", "Carrie-Anne Moss"), detalle.actores);
        assertEquals("https://ejemplo.com/matrix.jpg", detalle.imagenUrl);
        assertEquals(LocalDate.of(1999, 3, 31), detalle.fechaSalida);
    }

    @Test
    @DisplayName("No permite crear película con título vacío")
    void constructor_tituloVacio_lanzaExcepcion() {
        assertThrows(RuntimeException.class, () -> new Pelicula(
            "",
            "nuevo",
            List.of("Director"),
            1000.0,
            "DVD",
            "Acción",
            "Sinopsis",
            List.of("Actor"),
            "url",
            LocalDate.now()
        ));
    }

    @Test
    @DisplayName("No permite crear película con precio negativo")
    void constructor_precioNegativo_lanzaExcepcion() {
        assertThrows(RuntimeException.class, () -> new Pelicula(
            "Titulo",
            "nuevo",
            List.of("Director"),
            -10.0,
            "DVD",
            "Acción",
            "Sinopsis",
            List.of("Actor"),
            "url",
            LocalDate.now()
        ));
    }

    @Test
    @DisplayName("No permite crear película con condición inválida")
    void constructor_condicionInvalida_lanzaExcepcion() {
        assertThrows(RuntimeException.class, () -> new Pelicula(
            "Titulo",
            "reparado",
            List.of("Director"),
            1000.0,
            "DVD",
            "Acción",
            "Sinopsis",
            List.of("Actor"),
            "url",
            LocalDate.now()
        ));
    }
}
