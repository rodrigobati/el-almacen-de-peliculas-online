package unrn.infra.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)
public class EventoProcesadoRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public boolean existsById(String eventId) {
        return entityManager.find(EventoProcesadoEntity.class, eventId) != null;
    }

    @Transactional
    public void save(EventoProcesadoEntity eventoProcesado) {
        entityManager.persist(eventoProcesado);
    }

    public long count() {
        return entityManager.createQuery("SELECT COUNT(e) FROM EventoProcesadoEntity e", Long.class)
                .getSingleResult();
    }
}
