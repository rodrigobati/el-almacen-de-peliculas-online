package unrn.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ActorTest {

    @Test
    @DisplayName("Actor nombre válido se crea correctamente")
    void constructor_nombreValido_creaActor() {
        // Setup & ejercitación
        var actor = new Actor("Robert De Niro");

        // Verificación
        assertEquals("Robert De Niro", actor.nombre(), "El nombre del actor debe conservarse");
    }

    @Test
    @DisplayName("Constructor lanza excepción si nombre vacío")
    void constructor_nombreVacio_lanzaExcepcion() {
        var ex = assertThrows(RuntimeException.class, () -> new Actor(""));
        assertEquals(Actor.ERROR_NOMBRE, ex.getMessage());
    }
}
