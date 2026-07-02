package unrn.event.stock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Listener legado para compras confirmadas que descuenta o rechaza stock.
 *
 * Esta desactivado por property salvo que se habilite explicitamente. Permite
 * conservar compatibilidad con el evento directo de ventas mientras el flujo nuevo
 * usa solicitudes de validacion y resultados publicados por outbox.
 */
@Component
@ConditionalOnProperty(name = "catalogo.stock.legacy-compra-confirmada-listener.enabled", havingValue = "true", matchIfMissing = false)
public class CompraConfirmadaListener {

    private static final Logger log = LoggerFactory.getLogger(CompraConfirmadaListener.class);

    private final ProcesarCompraConfirmadaService procesarCompraConfirmadaService;

    /**
     * Inicializa una instancia de CompraConfirmadaListener con los datos necesarios.
     */
    public CompraConfirmadaListener(ProcesarCompraConfirmadaService procesarCompraConfirmadaService) {
        this.procesarCompraConfirmadaService = procesarCompraConfirmadaService;
    }

    /**
     * Consume eventos legacy de compra confirmada y delega su procesamiento.
     */
    @RabbitListener(queues = "catalogo.q.ventas-compra-confirmada")
    public void onCompraConfirmada(CompraConfirmadaEvent event) {
        try {
            var resultado = procesarCompraConfirmadaService.procesar(event);

            if (resultado.duplicado()) {
                log.info("Evento de compra ya procesado eventId={}", event.eventId());
                return;
            }

            log.info("Compra {} procesada en catalogo; resultado registrado en outbox", event.compraId());
        } catch (RuntimeException ex) {
            log.error("Error procesando compra confirmada eventId={} compraId={} mensaje={}",
                    event != null ? event.eventId() : "null",
                    event != null ? event.compraId() : null,
                    ex.getMessage());
            throw ex;
        }
    }
}
