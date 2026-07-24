package unrn.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas del objeto de dominio Actor.
 *
 * Verifican la regla de nombre obligatorio y el comportamiento basico de lectura
 * que usan peliculas, servicios administrativos y DTOs.
 */
class ActorTest {

    @Test
    @DisplayName("Actor nombre vÃ¡lido se crea correctamente")
    void constructor_nombreValido_creaActor() {
        // Setup & ejercitaciÃ³n
        var actor = new Actor("Robert De Niro");

        // VerificaciÃ³n
        assertEquals("Robert De Niro", actor.nombre(), "El nombre del actor debe conservarse");
    }

    @Test
    @DisplayName("Constructor lanza excepciÃ³n si nombre vacÃ­o")
    void constructor_nombreVacio_lanzaExcepcion() {
        var ex = assertThrows(RuntimeException.class, () -> new Actor(""));
        assertEquals(Actor.ERROR_NOMBRE, ex.getMessage());
    }
}
