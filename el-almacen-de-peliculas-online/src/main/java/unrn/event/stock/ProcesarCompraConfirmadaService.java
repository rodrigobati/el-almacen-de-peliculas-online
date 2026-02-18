package unrn.event.stock;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unrn.infra.persistence.EventoProcesadoEntity;
import unrn.infra.persistence.EventoProcesadoRepository;
import unrn.infra.persistence.PeliculaEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProcesarCompraConfirmadaService {

    static final String SOURCE_VENTAS = "ventas";
    static final String MOTIVO_STOCK_INSUFICIENTE = "STOCK_INSUFICIENTE";
    static final String MOTIVO_PELICULA_INEXISTENTE = "PELICULA_INEXISTENTE";

    private final EntityManager entityManager;
    private final EventoProcesadoRepository eventoProcesadoRepository;

    public ProcesarCompraConfirmadaService(EntityManager entityManager,
            EventoProcesadoRepository eventoProcesadoRepository) {
        this.entityManager = entityManager;
        this.eventoProcesadoRepository = eventoProcesadoRepository;
    }

    @Transactional
    public ResultadoProcesamiento procesar(CompraConfirmadaEvent event) {
        if (eventoProcesadoRepository.existsById(event.eventId())) {
            return ResultadoProcesamiento.eventoDuplicado();
        }

        List<ValidacionItem> validaciones = validarItems(event.items());

        if (existePeliculaInexistente(validaciones)) {
            registrarEventoProcesado(event);
            return ResultadoProcesamiento.conRechazo(new StockRechazadoEvent(
                    event.compraId(),
                    MOTIVO_PELICULA_INEXISTENTE,
                    mapearDetalles(validaciones)));
        }

        if (existeStockInsuficiente(validaciones)) {
            registrarEventoProcesado(event);
            return ResultadoProcesamiento.conRechazo(new StockRechazadoEvent(
                    event.compraId(),
                    MOTIVO_STOCK_INSUFICIENTE,
                    mapearDetalles(validaciones)));
        }

        for (ValidacionItem validacion : validaciones) {
            validacion.pelicula().descontarStock(validacion.solicitado());
        }

        registrarEventoProcesado(event);
        return ResultadoProcesamiento.procesadoSinRechazo();
    }

    private List<ValidacionItem> validarItems(List<CompraConfirmadaEvent.ItemCompraConfirmada> items) {
        List<ValidacionItem> validaciones = new ArrayList<>();

        for (CompraConfirmadaEvent.ItemCompraConfirmada item : items) {
            PeliculaEntity pelicula = entityManager.find(PeliculaEntity.class, item.peliculaId(),
                    LockModeType.PESSIMISTIC_WRITE);
            BigDecimal solicitado = BigDecimal.valueOf(item.cantidad());

            if (pelicula == null || !pelicula.estaActiva()) {
                validaciones.add(ValidacionItem.peliculaInexistente(item.peliculaId(), solicitado));
                continue;
            }

            if (pelicula.stockDisponible().compareTo(solicitado) < 0) {
                validaciones.add(ValidacionItem.stockInsuficiente(pelicula, solicitado));
                continue;
            }

            validaciones.add(ValidacionItem.ok(pelicula, solicitado));
        }

        return validaciones;
    }

    private boolean existePeliculaInexistente(List<ValidacionItem> validaciones) {
        return validaciones.stream().anyMatch(ValidacionItem::esPeliculaInexistente);
    }

    private boolean existeStockInsuficiente(List<ValidacionItem> validaciones) {
        return validaciones.stream().anyMatch(ValidacionItem::esStockInsuficiente);
    }

    private List<StockRechazadoEvent.DetalleStockRechazado> mapearDetalles(List<ValidacionItem> validaciones) {
        return validaciones.stream()
                .filter(ValidacionItem::esError)
                .map(validacion -> new StockRechazadoEvent.DetalleStockRechazado(
                        validacion.peliculaId(),
                        validacion.solicitado().intValue(),
                        validacion.disponibleComoString()))
                .toList();
    }

    private void registrarEventoProcesado(CompraConfirmadaEvent event) {
        eventoProcesadoRepository.save(new EventoProcesadoEntity(
                event.eventId(),
                Instant.now(),
                SOURCE_VENTAS,
                event.compraId()));
    }

    public record ResultadoProcesamiento(boolean duplicado, StockRechazadoEvent rechazoEvent) {
        public static ResultadoProcesamiento eventoDuplicado() {
            return new ResultadoProcesamiento(true, null);
        }

        public static ResultadoProcesamiento procesadoSinRechazo() {
            return new ResultadoProcesamiento(false, null);
        }

        public static ResultadoProcesamiento conRechazo(StockRechazadoEvent rechazoEvent) {
            return new ResultadoProcesamiento(false, rechazoEvent);
        }

        public boolean tieneRechazo() {
            return rechazoEvent != null;
        }
    }

    private record ValidacionItem(PeliculaEntity pelicula,
            Long peliculaId,
            BigDecimal solicitado,
            BigDecimal disponible,
            TipoValidacion tipoValidacion) {

        static ValidacionItem peliculaInexistente(Long peliculaId, BigDecimal solicitado) {
            return new ValidacionItem(null, peliculaId, solicitado, null, TipoValidacion.PELICULA_INEXISTENTE);
        }

        static ValidacionItem stockInsuficiente(PeliculaEntity pelicula, BigDecimal solicitado) {
            return new ValidacionItem(
                    pelicula,
                    pelicula.id(),
                    solicitado,
                    pelicula.stockDisponible(),
                    TipoValidacion.STOCK_INSUFICIENTE);
        }

        static ValidacionItem ok(PeliculaEntity pelicula, BigDecimal solicitado) {
            return new ValidacionItem(
                    pelicula,
                    pelicula.id(),
                    solicitado,
                    pelicula.stockDisponible(),
                    TipoValidacion.OK);
        }

        boolean esError() {
            return tipoValidacion != TipoValidacion.OK;
        }

        boolean esPeliculaInexistente() {
            return tipoValidacion == TipoValidacion.PELICULA_INEXISTENTE;
        }

        boolean esStockInsuficiente() {
            return tipoValidacion == TipoValidacion.STOCK_INSUFICIENTE;
        }

        String disponibleComoString() {
            return disponible == null ? null : disponible.stripTrailingZeros().toPlainString();
        }
    }

    private enum TipoValidacion {
        OK,
        PELICULA_INEXISTENTE,
        STOCK_INSUFICIENTE
    }
}
