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
 * Pruebas de integracion del servicio administrativo de actores.
 *
 * Validan altas, busquedas, normalizacion de paginacion y rechazo de nombres vacios
 * o duplicados antes de exponer actores al backoffice.
 */
@SpringBootTest(classes = Application.class, properties = {
        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost/jwks",
        "spring.rabbitmq.listener.simple.auto-startup=false",
        "spring.rabbitmq.listener.direct.auto-startup=false"
})
class ActorServiceTest {

    @Autowired
    private ActorService actorService;

    @Test
    @DisplayName("crear actor valido retorna actor con id")
    void crearActorValido_retornaActorConId() {
        // Setup
        String nombre = "Actor Test " + System.nanoTime();

        // EjercitaciÃ³n
        var creado = actorService.crear(nombre);

        // VerificaciÃ³n
        assertNotNull(creado.id(), "El actor creado debe tener id");
        assertEquals(nombre, creado.nombre(), "El nombre del actor creado debe coincidir");
    }

    @Test
    @DisplayName("crear actor vacio lanza error nombre requerido")
    void crearActorVacio_lanzaErrorNombreRequerido() {
        // Setup
        String nombre = "   ";

        // EjercitaciÃ³n
        var ex = assertThrows(ValidationRuntimeException.class, () -> actorService.crear(nombre));

        // VerificaciÃ³n
        assertEquals(ActorService.ERROR_NOMBRE_REQUERIDO, ex.getMessage(),
                "El mensaje debe coincidir con la constante de validaciÃ³n");
    }

    @Test
    @DisplayName("crear actor duplicado ignorando mayusculas lanza error")
    void crearActorDuplicadoIgnorandoMayusculas_lanzaErrorNombreDuplicado() {
        // Setup
        String base = "Duplicado Actor " + System.nanoTime();
        actorService.crear(base.toLowerCase());

        // EjercitaciÃ³n
        var ex = assertThrows(ValidationRuntimeException.class, () -> actorService.crear(base.toUpperCase()));

        // VerificaciÃ³n
        assertEquals(ActorService.ERROR_NOMBRE_DUPLICADO, ex.getMessage(),
                "El mensaje debe coincidir con la constante de duplicado");
    }

    @Test
    @DisplayName("buscar actores con q filtra resultados")
    void buscarActoresConQ_filtraResultados() {
        // Setup
        String token = "FiltroActor" + System.nanoTime();
        actorService.crear(token + " Uno");
        actorService.crear("Otro " + System.nanoTime());

        // EjercitaciÃ³n
        var resultado = actorService.buscar(token, null, null);

        // VerificaciÃ³n
        assertTrue(resultado.stream().anyMatch(item -> item.nombre().contains(token)),
                "La bÃºsqueda debe incluir el actor que contiene el texto buscado");
        assertTrue(resultado.stream().allMatch(item -> item.nombre().toLowerCase().contains(token.toLowerCase())),
                "Todos los resultados deben respetar el filtro por texto");
    }

    @Test
    @DisplayName("buscar actores sin coincidencias retorna lista vacia")
    void buscarActoresSinCoincidencias_retornaListaVacia() {
        // Setup
        String q = "NoExisteActor_" + System.nanoTime();

        // EjercitaciÃ³n
        var resultado = actorService.buscar(q, null, null);

        // VerificaciÃ³n
        assertTrue(resultado.isEmpty(), "Sin coincidencias el listado debe ser vacÃ­o");
    }

    @Test
    @DisplayName("buscar actores con page y size invalidos no lanza error")
    void buscarActoresConPageYSizeInvalidos_noLanzaError() {
        // Setup
        String nombre = "Actor Paginado " + System.nanoTime();
        actorService.crear(nombre);

        // EjercitaciÃ³n
        var resultado = assertDoesNotThrow(() -> actorService.buscar("Actor", -7, 0),
                "El servicio no debe lanzar excepciÃ³n con paginaciÃ³n invÃ¡lida");

        // VerificaciÃ³n
        assertTrue(resultado != null, "La bÃºsqueda debe devolver una lista vÃ¡lida");
    }
}
