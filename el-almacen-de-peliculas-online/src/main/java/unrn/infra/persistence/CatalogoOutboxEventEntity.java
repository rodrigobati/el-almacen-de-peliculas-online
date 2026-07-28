package unrn.infra.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Entidad JPA del outbox de eventos salientes del catalogo.
 *
 * Guarda payload, tipo de evento, aggregate id, estado, intentos y errores para que
 * la publicacion a RabbitMQ pueda hacerse de forma confiable despues de confirmar
 * la transaccion de negocio.
 */
@Entity
@Table(name = "catalogo_outbox_event", indexes = {
        @Index(name = "idx_catalogo_outbox_status_created", columnList = "status, created_at")
})
public class CatalogoOutboxEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "aggregate_type", nullable = false, length = 64)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false)
    private Long aggregateId;

    @Column(name = "event_id", nullable = false, length = 64)
    private String eventId;

    @Column(name = "event_type", nullable = false, length = 128)
    private String eventType;

    @Column(name = "payload_json", nullable = false, length = 8000)
    private String payloadJson;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private CatalogoOutboxEventStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "attempts", nullable = false)
    private int attempts;

    @Column(name = "last_error", length = 1000)
    private String lastError;

    /**
     * Inicializa una instancia de CatalogoOutboxEventEntity con los datos necesarios.
     */
    protected CatalogoOutboxEventEntity() {
    }

    /**
     * Inicializa una instancia de CatalogoOutboxEventEntity con los datos necesarios.
     */
    public CatalogoOutboxEventEntity(String aggregateType,
            Long aggregateId,
            String eventId,
            String eventType,
            String payloadJson,
            Instant createdAt) {
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventId = eventId;
        this.eventType = eventType;
        this.payloadJson = payloadJson;
        this.status = CatalogoOutboxEventStatus.PENDING;
        this.createdAt = createdAt;
        this.attempts = 0;
    }

    /**
     * Devuelve el valor de id.
     */
    public Long id() {
        return id;
    }

    /**
     * Devuelve el valor de eventType.
     */
    public String eventType() {
        return eventType;
    }

    /**
     * Devuelve el valor de payloadJson.
     */
    public String payloadJson() {
        return payloadJson;
    }

    /**
     * Devuelve el valor de status.
     */
    public CatalogoOutboxEventStatus status() {
        return status;
    }

    /**
     * Marca el evento outbox como publicado y limpia el ultimo error.
     */
    public void marcarPublicado(Instant publishedAt) {
        this.status = CatalogoOutboxEventStatus.PUBLISHED;
        this.publishedAt = publishedAt;
        this.lastError = null;
    }

    /**
     * Suma un intento fallido y marca el evento como FAILED si agoto reintentos.
     */
    public void registrarFallo(String error, int maxAttempts) {
        this.attempts += 1;
        this.lastError = truncar(error);
        if (this.attempts >= maxAttempts) {
            this.status = CatalogoOutboxEventStatus.FAILED;
        }
    }

    /**
     * Recorta el mensaje de error para que entre en la columna last_error del outbox.
     */
    private String truncar(String error) {
        if (error == null) {
            return null;
        }
        return error.length() <= 1000 ? error : error.substring(0, 1000);
    }
}
