package unrn.infra.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import unrn.model.Director;

import java.util.List;

/**
 * Repositorio de infraestructura para consultar y guardar directores.
 *
 * Centraliza las operaciones JPA que necesita el backoffice: obtener por id,
 * resolver listas de ids, buscar por texto, detectar duplicados y persistir nuevos
 * directores.
 */
@Repository
@Transactional(readOnly = true)
public class DirectorRepository {

    @PersistenceContext
    private EntityManager em;

    /**
     * Recupera directores por id y los convierte al modelo de dominio.
     */
    public List<Director> findAllById(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        var entidades = em
                .createQuery("SELECT d FROM DirectorEntity d WHERE d.id IN :ids", DirectorEntity.class)
                .setParameter("ids", ids)
                .getResultList();

        // Convertimos Entity â†’ Domain
        return entidades.stream()
                .map(entity -> entity.asDomain())
                .toList();
    }

    /**
     * Recupera un director por id y devuelve null si no existe.
     */
    public Director findById(Long id) {
        var de = em.find(DirectorEntity.class, id);
        return (de == null) ? null : de.asDomain();
    }

    /**
     * Recupera la entidad Director para operaciones de persistencia.
     */
    public DirectorEntity findEntityById(Long id) {
        return em.find(DirectorEntity.class, id);
    }

    /**
     * Detecta si ya existe un director con ese nombre ignorando mayusculas.
     */
    public boolean existsByNombreIgnoreCase(String nombre) {
        Long count = em
                .createQuery(
                        "SELECT COUNT(d) FROM DirectorEntity d WHERE LOWER(d.nombre) = LOWER(:nombre)",
                        Long.class)
                .setParameter("nombre", nombre)
                .getSingleResult();
        return count != null && count > 0;
    }

    /**
     * Busca entidades Director por coincidencia parcial de nombre.
     */
    public List<DirectorEntity> buscarPorNombre(String q, Integer page, Integer size) {
        String normalized = q == null ? "" : q.trim().toLowerCase();
        String filter = "%" + normalized + "%";

        var query = em.createQuery(
                "SELECT d FROM DirectorEntity d WHERE LOWER(d.nombre) LIKE :q ORDER BY d.nombre ASC",
                DirectorEntity.class)
                .setParameter("q", filter);

        if (page != null && size != null && size > 0 && page >= 0) {
            query.setFirstResult(page * size);
            query.setMaxResults(size);
        }

        return query.getResultList();
    }

    /**
     * Persiste un director nuevo y fuerza el flush para obtener su id.
     */
    @Transactional
    public DirectorEntity guardar(DirectorEntity directorEntity) {
        em.persist(directorEntity);
        em.flush();
        return directorEntity;
    }
}
