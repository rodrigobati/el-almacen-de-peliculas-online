package unrn.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DirectorGeneroFormatoCondicionTest {

    @Test
    @DisplayName("Director válido se crea correctamente")
    void director_constructor_valido() {
        var d = new Director("Quentin Tarantino");
        assertEquals("Quentin Tarantino", d.nombre(), "El nombre del director debe conservarse");
    }

    @Test
    @DisplayName("Director inválido lanza excepción con mensaje")
    void director_nombreVacio_lanzaExcepcion() {
        var ex = assertThrows(RuntimeException.class, () -> new Director(""));
        assertEquals(Director.ERROR_NOMBRE, ex.getMessage());
    }

    @Test
    @DisplayName("Genero válido se crea correctamente")
    void genero_constructor_valido() {
        var g = new Genero("Drama");
        assertEquals("Drama", g.nombre(), "El nombre del género debe conservarse");
    }

    @Test
    @DisplayName("Genero inválido lanza excepción con mensaje")
    void genero_nombreVacio_lanzaExcepcion() {
        var ex = assertThrows(RuntimeException.class, () -> new Genero(""));
        assertEquals(Genero.ERROR_NOMBRE, ex.getMessage());
    }

    @Test
    @DisplayName("Formato válido se crea correctamente")
    void formato_constructor_valido() {
        var f = new Formato("DVD");
        assertEquals("DVD", f.tipo(), "El tipo de formato debe conservarse");
    }

    @Test
    @DisplayName("Formato inválido lanza excepción con mensaje")
    void formato_tipoVacio_lanzaExcepcion() {
        var ex = assertThrows(RuntimeException.class, () -> new Formato(""));
        assertEquals(Formato.ERROR_TIPO, ex.getMessage());
    }

    @Test
    @DisplayName("Condicion acepta 'nuevo' y 'usado' y normaliza a minúsculas")
    void condicion_valida_nuevo_usado() {
        var c1 = new Condicion("nuevo");
        var c2 = new Condicion("USADO");
        assertEquals("nuevo", c1.valor());
        assertEquals("usado", c2.valor());
    }

    @Test
    @DisplayName("Condicion inválida lanza excepción con mensaje de error")
    void condicion_invalida_lanzaExcepcion() {
        var ex = assertThrows(RuntimeException.class, () -> new Condicion("reparado"));
        assertEquals(Condicion.ERROR_CONDICION, ex.getMessage());
    }
}
