package unrn.event.stock;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unrn.infra.persistence.CatalogoOutboxEventEntity;
import unrn.infra.persistence.CatalogoOutboxEventRepository;
import unrn.infra.persistence.CatalogoOutboxEventStatus;

import java.time.Instant;
import java.util.List;

/**
 * Servicio de aplicacion para registrar y administrar el outbox de stock.
 *
 * Serializa eventos aceptados o rechazados, los guarda como pendientes, permite
 * leerlos para publicacion y marca exito o fallo. Es la pieza que desacopla la
 * transaccion de stock de la entrega efectiva por RabbitMQ.
 */
@Service
public class StockValidationResultOutboxService {

    static final String AGGREGATE_TYPE_COMPRA = "COMPRA";
    static final String EVENT_TYPE_STOCK_VALIDATION_ACCEPTED = "StockValidationAcceptedEvent";
    static final String EVENT_TYPE_STOCK_RECHAZADO = "StockRechazadoEvent";
    static final String ERROR_EVENT_TYPE_NO_SOPORTADO = "Tipo de evento outbox no soportado";
    static final String ERROR_SERIALIZANDO_EVENTO = "No se pudo serializar evento de outbox";
    static final String ERROR_DESERIALIZANDO_EVENTO = "No se pudo deserializar evento de outbox";
    static final String ERROR_OUTBOX_NO_ENCONTRADO = "No se encontro evento outbox";

    private final CatalogoOutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    /**
     * Inicializa una instancia de StockValidationResultOutboxService con los datos necesarios.
     */
    public StockValidationResultOutboxService(CatalogoOutboxEventRepository outboxEventRepository,
            ObjectMapper objectMapper) {
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Guarda en outbox el resultado de stock aceptado para publicarlo luego.
     */
    @Transactional
    public void registrarAccepted(StockValidationAcceptedEvent event) {
        registrar(event.compraId(), event.eventId(), EVENT_TYPE_STOCK_VALIDATION_ACCEPTED, event);
    }

    /**
     * Guarda en outbox el resultado de stock rechazado para publicarlo luego.
     */
    @Transactional
    public void registrarRechazado(StockRechazadoEvent event) {
        registrar(event.compraId(), event.eventId(), EVENT_TYPE_STOCK_RECHAZADO, event);
    }

    /**
     * Obtiene los eventos outbox pendientes que aun pueden reintentarse.
     */
    public List<CatalogoOutboxEventEntity> pendientes(int maxAttempts) {
        return outboxEventRepository.findPendientes(maxAttempts);
    }

    /**
     * Deserializa el payload outbox segun el tipo de evento registrado.
     */
    public Object leerEvento(CatalogoOutboxEventEntity entity) {
        try {
            if (EVENT_TYPE_STOCK_VALIDATION_ACCEPTED.equals(entity.eventType())) {
                return objectMapper.readValue(entity.payloadJson(), StockValidationAcceptedEvent.class);
            }
            if (EVENT_TYPE_STOCK_RECHAZADO.equals(entity.eventType())) {
                return objectMapper.readValue(entity.payloadJson(), StockRechazadoEvent.class);
            }
            throw new RuntimeException(ERROR_EVENT_TYPE_NO_SOPORTADO + ": " + entity.eventType());
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ERROR_DESERIALIZANDO_EVENTO, ex);
        }
    }

    /**
     * Marca como publicado el evento outbox indicado.
     */
    @Transactional
    public void marcarPublicado(Long outboxId) {
        CatalogoOutboxEventEntity entity = buscar(outboxId);
        entity.marcarPublicado(Instant.now());
    }

    /**
     * Registra el fallo de publicacion de un evento outbox.
     */
    @Transactional
    public void registrarFallo(Long outboxId, String error, int maxAttempts) {
        CatalogoOutboxEventEntity entity = buscar(outboxId);
        entity.registrarFallo(error, maxAttempts);
    }

    /**
     * Cuenta cuantos eventos de outbox siguen pendientes de publicacion.
     */
    public long pendientesCount() {
        return outboxEventRepository.countByStatus(CatalogoOutboxEventStatus.PENDING);
    }

    /**
     * Crea la fila outbox con payload JSON y estado pendiente.
     */
    private void registrar(Long compraId, String eventId, String eventType, Object event) {
        outboxEventRepository.save(new CatalogoOutboxEventEntity(
                AGGREGATE_TYPE_COMPRA,
                compraId,
                eventId,
                eventType,
                serializar(event),
                Instant.now()));
    }

    /**
     * Serializa un evento a JSON para persistirlo en outbox.
     */
    private String serializar(Object event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ERROR_SERIALIZANDO_EVENTO, ex);
        }
    }

    /**
     * Busca el evento outbox requerido o falla si ya no existe.
     */
    private CatalogoOutboxEventEntity buscar(Long outboxId) {
        return outboxEventRepository.findById(outboxId)
                .orElseThrow(() -> new RuntimeException(ERROR_OUTBOX_NO_ENCONTRADO));
    }
}
