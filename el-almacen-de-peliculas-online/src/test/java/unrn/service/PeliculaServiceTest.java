package unrn.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import unrn.model.Pelicula;
import unrn.dto.DetallePeliculaDTO;
import unrn.infra.persistence.PeliculaRepository;

import java.time.LocalDate;
import java.util.List;

class PeliculaServiceTest {
    /*
     * @Test
     * 
     * @DisplayName("crear con DTO válido crea película correctamente")
     * void crear_dtoValido_creaPelicula() {
     * // Setup
     * var repository = new TestPeliculaRepository();
     * var service = new PeliculaService(repository);
     * var dto = new DetallePeliculaDTO(
     * "El Padrino",
     * "nuevo",
     * List.of("Francis Ford Coppola"),
     * 29.99,
     * "DVD",
     * "Drama",
     * "Una película sobre la mafia",
     * List.of("Marlon Brando", "Al Pacino"),
     * "url-imagen",
     * LocalDate.of(1972, 3, 24),
     * 5
     * );
     * 
     * // Ejercitar
     * service.crear(dto);
     * 
     * // Verificar
     * var peliculaCreada = repository.ultimaPeliculaGuardada;
     * assertNotNull(peliculaCreada, "La película debe ser guardada");
     * assertEquals("El Padrino", peliculaCreada.titulo());
     * assertEquals("nuevo", peliculaCreada.condicion().valor());
     * assertEquals(29.99, peliculaCreada.precio());
     * assertEquals("DVD", peliculaCreada.formato().tipo());
     * assertEquals("Drama", peliculaCreada.genero().nombre());
     * assertEquals(1, peliculaCreada.directores().size());
     * assertEquals("Francis Ford Coppola",
     * peliculaCreada.directores().get(0).nombre());
     * assertEquals(2, peliculaCreada.actores().size());
     * assertTrue(peliculaCreada.actores().stream().anyMatch(a ->
     * a.nombre().equals("Marlon Brando")));
     * assertTrue(peliculaCreada.actores().stream().anyMatch(a ->
     * a.nombre().equals("Al Pacino")));
     * }
     * 
     * // Repositorio de prueba que guarda la última película
     * private static class TestPeliculaRepository extends PeliculaRepository {
     * Pelicula ultimaPeliculaGuardada;
     * 
     * @Override
     * public Long guardar(Pelicula p) {
     * this.ultimaPeliculaGuardada = p;
     * return 1L;
     * }
     * }
     */
}