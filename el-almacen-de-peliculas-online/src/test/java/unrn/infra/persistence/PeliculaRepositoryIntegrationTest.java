package unrn.infra.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import unrn.model.*;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

import unrn.app.Application;

@SpringBootTest(classes = Application.class)
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

    private Pelicula samplePelicula() {
        var d = java.util.List.of(new Director("Pedro"));
        var a = java.util.List.of(new Actor("Ana"));
        return new Pelicula(
                "La prueba",
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
