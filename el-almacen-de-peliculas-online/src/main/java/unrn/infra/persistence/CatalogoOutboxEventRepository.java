package unrn.infra.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio del outbox de eventos pendientes de publicacion.
 *
 * Permite registrar eventos nuevos, buscar pendientes con limite de intentos,
 * contar por estado y actualizar el resultado de la publicacion sin exponer
 * EntityManager al servicio de outbox.
 */
@Repository
@Transactional(readOnly = true)
public class CatalogoOutboxEventRepository {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Persiste un evento outbox pendiente de publicacion.
     */
    @Transactional
    public void save(CatalogoOutboxEventEntity event) {
        entityManager.persist(event);
    }

    /**
     * Busca un evento outbox por id para actualizar su estado.
     */
    public Optional<CatalogoOutboxEventEntity> findById(Long id) {
        return Optional.ofNullable(entityManager.find(CatalogoOutboxEventEntity.class, id));
    }

    /**
     * Lista eventos outbox pendientes que aun no agotaron sus intentos.
     */
    public List<CatalogoOutboxEventEntity> findPendientes(int maxAttempts) {
        return entityManager
                .createQuery("""
                        SELECT event
                        FROM CatalogoOutboxEventEntity event
                        WHERE event.status = :status
                          AND event.attempts < :maxAttempts
                        ORDER BY event.createdAt ASC
                        """, CatalogoOutboxEventEntity.class)
                .setParameter("status", CatalogoOutboxEventStatus.PENDING)
                .setParameter("maxAttempts", maxAttempts)
                .setMaxResults(100)
                .getResultList();
    }

    /**
     * Cuenta eventos outbox por estado de publicacion.
     */
    public long countByStatus(CatalogoOutboxEventStatus status) {
        return entityManager
                .createQuery("""
                        SELECT COUNT(event)
                        FROM CatalogoOutboxEventEntity event
                        WHERE event.status = :status
                        """, Long.class)
                .setParameter("status", status)
                .getSingleResult();
    }
}
