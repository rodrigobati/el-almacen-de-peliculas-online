package unrn.infra.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Repositorio para consultar y persistir marcas de eventos procesados.
 *
 * Lo usa el flujo de stock para saber si un evento de ventas ya fue atendido y para
 * guardar la marca transaccional que protege la idempotencia del procesamiento.
 */
@Repository
@Transactional(readOnly = true)
public class EventoProcesadoRepository {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Detecta si un eventId ya fue procesado para mantener idempotencia.
     */
    public boolean existsById(String eventId) {
        return entityManager.find(EventoProcesadoEntity.class, eventId) != null;
    }

    /**
     * Guarda el eventId procesado para evitar reprocesar el mismo mensaje.
     */
    @Transactional
    public void save(EventoProcesadoEntity eventoProcesado) {
        entityManager.persist(eventoProcesado);
    }

    /**
     * Cuenta eventos procesados registrados en la tabla de idempotencia.
     */
    public long count() {
        return entityManager.createQuery("SELECT COUNT(e) FROM EventoProcesadoEntity e", Long.class)
                .getSingleResult();
    }
}
