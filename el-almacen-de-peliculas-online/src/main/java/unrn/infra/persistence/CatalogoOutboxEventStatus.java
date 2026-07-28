package unrn.infra.persistence;

/**
 * Estados posibles de un evento guardado en el outbox del catalogo.
 *
 * Permiten distinguir eventos pendientes de publicar, publicados correctamente o
 * fallidos luego de agotar intentos, para que el scheduler tome decisiones claras.
 */
public enum CatalogoOutboxEventStatus {
    PENDING,
    PUBLISHED,
    FAILED
}
