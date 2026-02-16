package unrn.infra.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import unrn.model.*;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

import unrn.app.Application;

@SpringBootTest(classes = Application.class, properties = {
        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost/jwks",
        "spring.rabbitmq.listener.simple.auto-startup=false",
        "spring.rabbitmq.listener.direct.auto-startup=false"
})
class PeliculaRepositoryIntegrationTest {

    @Autowired
    PeliculaRepository repo;

    @Test
    @DisplayName("guardar y recuperar pelicula desde H2 via Spring Data JPA")
    void guardar_recuperar_pelicula() {
        var p = samplePelicula();
        var guardada = repo.guardar(p);
        assertNotNull(guardada, "La película guardada no debe ser nula");
        assertNotNull(guardada.id(), "El id devuelto no debe ser nulo");

        var found = repo.porId(guardada.id());
        assertNotNull(found, "La pelicula persistida debe recuperarse");
        assertEquals(p.titulo(), found.titulo(), "El titulo debe coincidir");
        assertEquals(p.precio(), found.precio(), "El precio debe coincidir");
    }

    @Test
    @DisplayName("listar todos excluye peliculas retiradas")
    void listarTodos_excluyePeliculasRetiradas() {
        // Setup
        var activa = repo.guardar(samplePelicula("Activa " + System.nanoTime()));
        var retirada = repo.guardar(samplePelicula("Retirada " + System.nanoTime()));
        repo.eliminar(retirada.id());

        // Ejercitación
        var items = repo.listarTodos();

        // Verificación
        assertTrue(items.stream().anyMatch(p -> p.id().equals(activa.id())),
                "La película activa debe aparecer en el listado");
        assertFalse(items.stream().anyMatch(p -> p.id().equals(retirada.id())),
                "La película retirada no debe aparecer en el listado");
    }

    @Test
    @DisplayName("buscar paginado excluye peliculas retiradas")
    void buscarPaginado_excluyePeliculasRetiradas() {
        // Setup
        String token = "FiltroRetiro" + System.nanoTime();
        var activa = repo.guardar(samplePelicula(token + " Activa"));
        var retirada = repo.guardar(samplePelicula(token + " Retirada"));
        repo.eliminar(retirada.id());

        // Ejercitación
        var page = repo.buscarPaginado(
                token,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                0,
                20,
                "titulo",
                true);

        // Verificación
        assertTrue(page.getItems().stream().anyMatch(p -> p.id().equals(activa.id())),
                "La película activa debe estar presente");
        assertFalse(page.getItems().stream().anyMatch(p -> p.id().equals(retirada.id())),
                "La película retirada debe quedar excluida");
    }

    private Pelicula samplePelicula() {
        return samplePelicula("La prueba");
    }

    private Pelicula samplePelicula(String titulo) {
        var d = java.util.List.of(new Director("Pedro"));
        var a = java.util.List.of(new Actor("Ana"));
        return new Pelicula(
                titulo,
                new Condicion("nuevo"),
                d,
                9.99,
                new Formato("Blu-ray"),
                new Genero("Comedia"),
                "Una sinopsis",
                a,
                "http://example.com/img.jpg",
                LocalDate.now(),
                4);
    }
}
