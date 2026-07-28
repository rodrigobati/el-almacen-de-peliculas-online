package unrn.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import unrn.app.Application;

/**
 * Pruebas de integracion del servicio administrativo de directores.
 *
 * Validan altas, busquedas, normalizacion de paginacion y rechazo de nombres vacios
 * o duplicados antes de asociar directores a peliculas.
 */
@SpringBootTest(classes = Application.class, properties = {
        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost/jwks",
        "spring.rabbitmq.listener.simple.auto-startup=false",
        "spring.rabbitmq.listener.direct.auto-startup=false"
})
class DirectorServiceTest {

    @Autowired
    private DirectorService directorService;

    @Test
    @DisplayName("crear director valido retorna director con id")
    void crearDirectorValido_retornaDirectorConId() {
        // Setup
        String nombre = "Director Test " + System.nanoTime();

        // EjercitaciÃ³n
        var creado = directorService.crear(nombre);

        // VerificaciÃ³n
        assertNotNull(creado.id(), "El director creado debe tener id");
        assertEquals(nombre, creado.nombre(), "El nombre del director creado debe coincidir");
    }

    @Test
    @DisplayName("crear director vacio lanza error nombre requerido")
    void crearDirectorVacio_lanzaErrorNombreRequerido() {
        // Setup
        String nombre = "   ";

        // EjercitaciÃ³n
        var ex = assertThrows(ValidationRuntimeException.class, () -> directorService.crear(nombre));

        // VerificaciÃ³n
        assertEquals(DirectorService.ERROR_NOMBRE_REQUERIDO, ex.getMessage(),
                "El mensaje debe coincidir con la constante de validaciÃ³n");
    }

    @Test
    @DisplayName("crear director duplicado ignorando mayusculas lanza error")
    void crearDirectorDuplicadoIgnorandoMayusculas_lanzaErrorNombreDuplicado() {
        // Setup
        String base = "Duplicado Director " + System.nanoTime();
        directorService.crear(base.toLowerCase());

        // EjercitaciÃ³n
        var ex = assertThrows(ValidationRuntimeException.class, () -> directorService.crear(base.toUpperCase()));

        // VerificaciÃ³n
        assertEquals(DirectorService.ERROR_NOMBRE_DUPLICADO, ex.getMessage(),
                "El mensaje debe coincidir con la constante de duplicado");
    }

    @Test
    @DisplayName("buscar directores con q filtra resultados")
    void buscarDirectoresConQ_filtraResultados() {
        // Setup
        String token = "FiltroDirector" + System.nanoTime();
        directorService.crear(token + " Uno");
        directorService.crear("Otro " + System.nanoTime());

        // EjercitaciÃ³n
        var resultado = directorService.buscar(token, null, null);

        // VerificaciÃ³n
        assertTrue(resultado.stream().anyMatch(item -> item.nombre().contains(token)),
                "La bÃºsqueda debe incluir el director que contiene el texto buscado");
        assertTrue(resultado.stream().allMatch(item -> item.nombre().toLowerCase().contains(token.toLowerCase())),
                "Todos los resultados deben respetar el filtro por texto");
    }

    @Test
    @DisplayName("buscar directores sin coincidencias retorna lista vacia")
    void buscarDirectoresSinCoincidencias_retornaListaVacia() {
        // Setup
        String q = "NoExisteDirector_" + System.nanoTime();

        // EjercitaciÃ³n
        var resultado = directorService.buscar(q, null, null);

        // VerificaciÃ³n
        assertTrue(resultado.isEmpty(), "Sin coincidencias el listado debe ser vacÃ­o");
    }

    @Test
    @DisplayName("buscar directores con page y size invalidos no lanza error")
    void buscarDirectoresConPageYSizeInvalidos_noLanzaError() {
        // Setup
        String nombre = "Director Paginado " + System.nanoTime();
        directorService.crear(nombre);

        // EjercitaciÃ³n
        var resultado = assertDoesNotThrow(() -> directorService.buscar("Director", -5, 0),
                "El servicio no debe lanzar excepciÃ³n con paginaciÃ³n invÃ¡lida");

        // VerificaciÃ³n
        assertTrue(resultado != null, "La bÃºsqueda debe devolver una lista vÃ¡lida");
    }
}
