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

        // Ejercitación
        var creado = actorService.crear(nombre);

        // Verificación
        assertNotNull(creado.id(), "El actor creado debe tener id");
        assertEquals(nombre, creado.nombre(), "El nombre del actor creado debe coincidir");
    }

    @Test
    @DisplayName("crear actor vacio lanza error nombre requerido")
    void crearActorVacio_lanzaErrorNombreRequerido() {
        // Setup
        String nombre = "   ";

        // Ejercitación
        var ex = assertThrows(ValidationRuntimeException.class, () -> actorService.crear(nombre));

        // Verificación
        assertEquals(ActorService.ERROR_NOMBRE_REQUERIDO, ex.getMessage(),
                "El mensaje debe coincidir con la constante de validación");
    }

    @Test
    @DisplayName("crear actor duplicado ignorando mayusculas lanza error")
    void crearActorDuplicadoIgnorandoMayusculas_lanzaErrorNombreDuplicado() {
        // Setup
        String base = "Duplicado Actor " + System.nanoTime();
        actorService.crear(base.toLowerCase());

        // Ejercitación
        var ex = assertThrows(ValidationRuntimeException.class, () -> actorService.crear(base.toUpperCase()));

        // Verificación
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

        // Ejercitación
        var resultado = actorService.buscar(token, null, null);

        // Verificación
        assertTrue(resultado.stream().anyMatch(item -> item.nombre().contains(token)),
                "La búsqueda debe incluir el actor que contiene el texto buscado");
        assertTrue(resultado.stream().allMatch(item -> item.nombre().toLowerCase().contains(token.toLowerCase())),
                "Todos los resultados deben respetar el filtro por texto");
    }

    @Test
    @DisplayName("buscar actores sin coincidencias retorna lista vacia")
    void buscarActoresSinCoincidencias_retornaListaVacia() {
        // Setup
        String q = "NoExisteActor_" + System.nanoTime();

        // Ejercitación
        var resultado = actorService.buscar(q, null, null);

        // Verificación
        assertTrue(resultado.isEmpty(), "Sin coincidencias el listado debe ser vacío");
    }

    @Test
    @DisplayName("buscar actores con page y size invalidos no lanza error")
    void buscarActoresConPageYSizeInvalidos_noLanzaError() {
        // Setup
        String nombre = "Actor Paginado " + System.nanoTime();
        actorService.crear(nombre);

        // Ejercitación
        var resultado = assertDoesNotThrow(() -> actorService.buscar("Actor", -7, 0),
                "El servicio no debe lanzar excepción con paginación inválida");

        // Verificación
        assertTrue(resultado != null, "La búsqueda debe devolver una lista válida");
    }
}
