package unrn.infra.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import unrn.model.Actor;

import java.util.List;

@Repository
@Transactional(readOnly = true)
public class ActorRepository {

    @PersistenceContext
    private EntityManager em;

    public List<Actor> findAllById(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        var entidades = em
                .createQuery("SELECT a FROM ActorEntity a WHERE a.id IN :ids", ActorEntity.class)
                .setParameter("ids", ids)
                .getResultList();

        // 👇 acá convertimos Entity → dominio
        return entidades.stream()
                .map(ActorEntity::asDomain)
                .toList();
    }

    public Actor findById(Long id) {
        var ae = em.find(ActorEntity.class, id);
        return (ae == null) ? null : ae.asDomain();
    }

    public ActorEntity findEntityById(Long id) {
        return em.find(ActorEntity.class, id);
    }

    public boolean existsByNombreIgnoreCase(String nombre) {
        Long count = em
                .createQuery(
                        "SELECT COUNT(a) FROM ActorEntity a WHERE LOWER(a.nombre) = LOWER(:nombre)",
                        Long.class)
                .setParameter("nombre", nombre)
                .getSingleResult();
        return count != null && count > 0;
    }

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

    @Transactional
    public ActorEntity guardar(ActorEntity actorEntity) {
        em.persist(actorEntity);
        em.flush();
        return actorEntity;
    }
}
