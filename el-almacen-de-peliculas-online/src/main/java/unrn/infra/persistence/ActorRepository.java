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
}
