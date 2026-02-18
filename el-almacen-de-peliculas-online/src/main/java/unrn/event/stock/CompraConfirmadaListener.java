package unrn.event.stock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class CompraConfirmadaListener {

    private static final Logger log = LoggerFactory.getLogger(CompraConfirmadaListener.class);

    private final ProcesarCompraConfirmadaService procesarCompraConfirmadaService;
    private final StockRechazadoPublisher stockRechazadoPublisher;

    public CompraConfirmadaListener(ProcesarCompraConfirmadaService procesarCompraConfirmadaService,
            StockRechazadoPublisher stockRechazadoPublisher) {
        this.procesarCompraConfirmadaService = procesarCompraConfirmadaService;
        this.stockRechazadoPublisher = stockRechazadoPublisher;
    }

    @RabbitListener(queues = "catalogo.q.ventas-compra-confirmada")
    public void onCompraConfirmada(CompraConfirmadaEvent event) {
        try {
            var resultado = procesarCompraConfirmadaService.procesar(event);

            if (resultado.duplicado()) {
                log.info("Evento de compra ya procesado eventId={}", event.eventId());
                return;
            }

            if (resultado.tieneRechazo()) {
                stockRechazadoPublisher.publicarAfterCommit(resultado.rechazoEvent());
                log.info("Compra {} rechazada por stock en catálogo", event.compraId());
                return;
            }

            log.info("Compra {} procesada correctamente en catálogo", event.compraId());
        } catch (RuntimeException ex) {
            log.error("Error procesando compra confirmada eventId={} compraId={} mensaje={}",
                    event.eventId(), event.compraId(), ex.getMessage());
        }
    }
}
