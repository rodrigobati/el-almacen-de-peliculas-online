package unrn.infra.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import unrn.model.Pelicula;

public interface PeliculaJpaRepository extends JpaRepository<Pelicula, Long> {
}