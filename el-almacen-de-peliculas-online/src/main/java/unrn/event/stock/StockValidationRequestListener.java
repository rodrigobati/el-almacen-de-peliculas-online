package unrn.event.stock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Listener RabbitMQ del flujo nuevo de validacion de stock.
 *
 * Recibe solicitudes de ventas, las adapta al modelo interno de compra confirmada y
 * delega el procesamiento transaccional. La respuesta se deja en outbox para ser
 * publicada de forma confiable.
 */
@Component
public class StockValidationRequestListener {

    private static final Logger log = LoggerFactory.getLogger(StockValidationRequestListener.class);

    private final ProcesarCompraConfirmadaService procesarCompraConfirmadaService;

    /**
     * Inicializa una instancia de StockValidationRequestListener con los datos necesarios.
     */
    public StockValidationRequestListener(ProcesarCompraConfirmadaService procesarCompraConfirmadaService) {
        this.procesarCompraConfirmadaService = procesarCompraConfirmadaService;
    }

    /**
     * Consume solicitudes de validacion de stock y delega su procesamiento.
     */
    @RabbitListener(queues = "${rabbitmq.catalogo.stock.validation.requested.queue:catalogo.stock.validation.requests}")
    public void onStockValidationRequested(StockValidationRequestedEvent event) {
        try {
            CompraConfirmadaEvent eventoMapeado = mapearACompraConfirmadaEvent(event);
            var resultado = procesarCompraConfirmadaService.procesar(eventoMapeado);

            if (resultado.duplicado()) {
                log.info("Solicitud de validacion ya procesada eventId={}", event.eventId());
                return;
            }

            if (resultado.tieneRechazo()) {
                log.info("Compra {} rechazada por stock en catalogo; resultado registrado en outbox", event.compraId());
                return;
            }

            log.info("Compra {} validada y descontada en catalogo; resultado registrado en outbox", event.compraId());
        } catch (RuntimeException ex) {
            log.error("Error procesando solicitud de validacion eventId={} compraId={} mensaje={}",
                    event != null ? event.eventId() : "null",
                    event != null ? event.compraId() : null,
                    ex.getMessage());
            throw ex;
        }
    }

    /**
     * Adapta el contrato de validacion de stock al contrato interno de compra.
     */
    private CompraConfirmadaEvent mapearACompraConfirmadaEvent(StockValidationRequestedEvent event) {
        List<CompraConfirmadaEvent.ItemCompraConfirmada> items = event.items().stream()
                .map(item -> new CompraConfirmadaEvent.ItemCompraConfirmada(item.peliculaId(), item.cantidad()))
                .toList();

        return new CompraConfirmadaEvent(
                event.eventId(),
                event.compraId(),
                "ventas",
                event.occurredAt(),
                items);
    }
}
