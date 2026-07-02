package unrn.infra.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import unrn.model.Actor;

import java.util.List;

/**
 * Repositorio de infraestructura para consultar y guardar actores.
 *
 * Encapsula EntityManager y consultas JPA usadas por servicios administrativos,
 * incluyendo busqueda por nombre, validacion de duplicados y conversion hacia el
 * objeto de dominio Actor cuando la capa de aplicacion lo necesita.
 */
@Repository
@Transactional(readOnly = true)
public class ActorRepository {

    @PersistenceContext
    private EntityManager em;

    /**
     * Recupera actores por id y los convierte al modelo de dominio.
     */
    public List<Actor> findAllById(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        var entidades = em
                .createQuery("SELECT a FROM ActorEntity a WHERE a.id IN :ids", ActorEntity.class)
                .setParameter("ids", ids)
                .getResultList();

        // ðŸ‘‡ acÃ¡ convertimos Entity â†’ dominio
        return entidades.stream()
                .map(ActorEntity::asDomain)
                .toList();
    }

    /**
     * Recupera un actor por id y devuelve null si no existe.
     */
    public Actor findById(Long id) {
        var ae = em.find(ActorEntity.class, id);
        return (ae == null) ? null : ae.asDomain();
    }

    /**
     * Recupera la entidad Actor para operaciones de persistencia.
     */
    public ActorEntity findEntityById(Long id) {
        return em.find(ActorEntity.class, id);
    }

    /**
     * Detecta si ya existe un actor con ese nombre ignorando mayusculas.
     */
    public boolean existsByNombreIgnoreCase(String nombre) {
        Long count = em
                .createQuery(
                        "SELECT COUNT(a) FROM ActorEntity a WHERE LOWER(a.nombre) = LOWER(:nombre)",
                        Long.class)
                .setParameter("nombre", nombre)
                .getSingleResult();
        return count != null && count > 0;
    }

    /**
     * Busca entidades Actor por coincidencia parcial de nombre.
     */
    public List<ActorEntity> buscarPorNombre(String q, Integer page, Integer size) {
        String normalized = q == null ? "" : q.trim().toLowerCase();
        String filter = "%" + normalized + "%";

        var query = em.createQuery(
                "SELECT a FROM ActorEntity a WHERE LOWER(a.nombre) LIKE :q ORDER BY a.nombre ASC",
                ActorEntity.class)
                .setParameter("q", filter);

        if (page != null && size != null && size > 0 && page >= 0) {
            query.setFirstResult(page * size);
            query.setMaxResults(size);
        }

        return query.getResultList();
    }

    /**
     * Persiste un actor nuevo y fuerza el flush para obtener su id.
     */
    @Transactional
    public ActorEntity guardar(ActorEntity actorEntity) {
        em.persist(actorEntity);
        em.flush();
        return actorEntity;
    }
}
