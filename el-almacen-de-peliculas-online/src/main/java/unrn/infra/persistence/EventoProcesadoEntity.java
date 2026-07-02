package unrn.infra.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Entidad JPA que registra eventos externos ya procesados.
 *
 * Se usa para idempotencia: si una compra o solicitud de stock llega repetida, el
 * sistema puede reconocer el eventId y evitar descontar stock o publicar resultados
 * dos veces.
 */
@Entity
@Table(name = "eventos_procesados", indexes = {
        @Index(name = "idx_eventos_procesados_compra", columnList = "compra_id")
})
public class EventoProcesadoEntity {

    @Id
    @Column(name = "event_id", nullable = false, length = 64)
    private String eventId;

    @Column(name = "processed_at", nullable = false)
    private Instant processedAt;

    @Column(name = "source", nullable = false, length = 32)
    private String source;

    @Column(name = "compra_id")
    private Long compraId;

    /**
     * Inicializa una instancia de EventoProcesadoEntity con los datos necesarios.
     */
    protected EventoProcesadoEntity() {
    }

    /**
     * Inicializa una instancia de EventoProcesadoEntity con los datos necesarios.
     */
    public EventoProcesadoEntity(String eventId, Instant processedAt, String source, Long compraId) {
        this.eventId = eventId;
        this.processedAt = processedAt;
        this.source = source;
        this.compraId = compraId;
    }
}
