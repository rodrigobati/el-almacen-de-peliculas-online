package unrn;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for simple App.
 */
public class AppTest {

    @Test
    @DisplayName("El test siempre debe pasar")
    void testAppSiempreTrue() {
        // Setup: No se requiere preparación para este test
        // Ejercitación: No hay acción a probar, solo se verifica true
        // Verificación: El resultado esperado es true
        assertTrue(true, "El valor esperado es true");
    }
}
