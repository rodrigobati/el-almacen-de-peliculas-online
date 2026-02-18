package unrn.infra.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;

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

    protected EventoProcesadoEntity() {
    }

    public EventoProcesadoEntity(String eventId, Instant processedAt, String source, Long compraId) {
        this.eventId = eventId;
        this.processedAt = processedAt;
        this.source = source;
        this.compraId = compraId;
    }
}
