package unrn.infra.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import unrn.model.Pelicula;

/**
 * Contrato Spring Data directo para Pelicula conservado como artefacto auxiliar.
 *
 * La persistencia activa del catalogo se concentra en PeliculaRepository y
 * PeliculaEntity. Esta interfaz no agrega consultas propias y conviene verla como
 * pieza heredada antes que como el repositorio principal de la vertical.
 */
public interface PeliculaJpaRepository extends JpaRepository<Pelicula, Long> {
}
