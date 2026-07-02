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

/**
 * Servicio transaccional que valida y aplica stock para una compra.
 *
 * Garantiza idempotencia por eventId, bloquea peliculas con lock pesimista,
 * detecta cantidades invalidas, peliculas inexistentes o stock insuficiente, y
 * registra en outbox el resultado aceptado o rechazado para publicarlo luego.
 */
@Service
public class ProcesarCompraConfirmadaService {

    static final String SOURCE_VENTAS = "ventas";
    static final String MOTIVO_STOCK_INSUFICIENTE = "STOCK_INSUFICIENTE";
    static final String MOTIVO_PELICULA_INEXISTENTE = "PELICULA_INEXISTENTE";
    static final String MOTIVO_CANTIDAD_INVALIDA = "CANTIDAD_INVALIDA";

    private final EntityManager entityManager;
    private final EventoProcesadoRepository eventoProcesadoRepository;
    private final StockValidationResultOutboxService outboxService;

    /**
     * Inicializa una instancia de ProcesarCompraConfirmadaService con los datos necesarios.
     */
    public ProcesarCompraConfirmadaService(EntityManager entityManager,
            EventoProcesadoRepository eventoProcesadoRepository,
            StockValidationResultOutboxService outboxService) {
        this.entityManager = entityManager;
        this.eventoProcesadoRepository = eventoProcesadoRepository;
        this.outboxService = outboxService;
    }

    /**
     * Procesa una compra o solicitud de stock de forma transaccional e idempotente.
     */
    @Transactional
    public ResultadoProcesamiento procesar(CompraConfirmadaEvent event) {
        if (eventoProcesadoRepository.existsById(event.eventId())) {
            return ResultadoProcesamiento.eventoDuplicado();
        }

        List<ValidacionItem> validaciones = validarItems(event.items());

        if (existeCantidadInvalida(validaciones)) {
            StockRechazadoEvent rechazoEvent = crearRechazo(event, MOTIVO_CANTIDAD_INVALIDA, validaciones);
            registrarEventoProcesado(event);
            outboxService.registrarRechazado(rechazoEvent);
            return ResultadoProcesamiento.conRechazo(rechazoEvent);
        }

        if (existePeliculaInexistente(validaciones)) {
            StockRechazadoEvent rechazoEvent = crearRechazo(event, MOTIVO_PELICULA_INEXISTENTE, validaciones);
            registrarEventoProcesado(event);
            outboxService.registrarRechazado(rechazoEvent);
            return ResultadoProcesamiento.conRechazo(rechazoEvent);
        }

        if (existeStockInsuficiente(validaciones)) {
            StockRechazadoEvent rechazoEvent = crearRechazo(event, MOTIVO_STOCK_INSUFICIENTE, validaciones);
            registrarEventoProcesado(event);
            outboxService.registrarRechazado(rechazoEvent);
            return ResultadoProcesamiento.conRechazo(rechazoEvent);
        }

        for (ValidacionItem validacion : validaciones) {
            validacion.pelicula().descontarStock(validacion.solicitado());
        }

        registrarEventoProcesado(event);
        StockValidationAcceptedEvent acceptedEvent = new StockValidationAcceptedEvent(
                event.eventId(),
                event.compraId(),
                event.fechaHora());
        outboxService.registrarAccepted(acceptedEvent);
        return ResultadoProcesamiento.procesadoSinRechazo(acceptedEvent);
    }

    /**
     * Valida cada item de la compra contra existencia, actividad y stock disponible.
     */
    private List<ValidacionItem> validarItems(List<CompraConfirmadaEvent.ItemCompraConfirmada> items) {
        List<ValidacionItem> validaciones = new ArrayList<>();

        for (CompraConfirmadaEvent.ItemCompraConfirmada item : items) {
            BigDecimal solicitado = BigDecimal.valueOf(item.cantidad());

            if (solicitado.compareTo(BigDecimal.ZERO) <= 0) {
                validaciones.add(ValidacionItem.cantidadInvalida(item.peliculaId(), solicitado));
                continue;
            }

            PeliculaEntity pelicula = entityManager.find(PeliculaEntity.class, item.peliculaId(),
                    LockModeType.PESSIMISTIC_WRITE);

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

    /**
     * Detecta si algun item solicita una cantidad menor o igual a cero.
     */
    private boolean existeCantidadInvalida(List<ValidacionItem> validaciones) {
        return validaciones.stream().anyMatch(ValidacionItem::esCantidadInvalida);
    }

    /**
     * Detecta si algun item apunta a una pelicula inexistente o inactiva.
     */
    private boolean existePeliculaInexistente(List<ValidacionItem> validaciones) {
        return validaciones.stream().anyMatch(ValidacionItem::esPeliculaInexistente);
    }

    /**
     * Detecta si alguna pelicula no tiene stock suficiente para la compra.
     */
    private boolean existeStockInsuficiente(List<ValidacionItem> validaciones) {
        return validaciones.stream().anyMatch(ValidacionItem::esStockInsuficiente);
    }

    /**
     * Convierte validaciones con error en detalles para el evento de rechazo.
     */
    private List<StockRechazadoEvent.DetalleStockRechazado> mapearDetalles(List<ValidacionItem> validaciones) {
        return validaciones.stream()
                .filter(ValidacionItem::esError)
                .map(validacion -> new StockRechazadoEvent.DetalleStockRechazado(
                        validacion.peliculaId(),
                        validacion.solicitado().intValue(),
                        validacion.disponibleComoString()))
                .toList();
    }

    /**
     * Registra el eventId procesado para garantizar idempotencia.
     */
    private void registrarEventoProcesado(CompraConfirmadaEvent event) {
        eventoProcesadoRepository.save(new EventoProcesadoEntity(
                event.eventId(),
                Instant.now(),
                SOURCE_VENTAS,
                event.compraId()));
    }

    /**
     * Construye el evento de rechazo de stock con motivo y detalles.
     */
    private StockRechazadoEvent crearRechazo(CompraConfirmadaEvent event,
            String motivo,
            List<ValidacionItem> validaciones) {
        return new StockRechazadoEvent(
                event.eventId(),
                event.compraId(),
                motivo,
                mapearDetalles(validaciones));
    }

    /**
     * Resultado interno del procesamiento de una compra o validacion de stock.
     *
     * Indica si el evento era duplicado y, cuando fue procesado, conserva el evento
     * aceptado o rechazado que debera publicarse hacia ventas.
     */
    public record ResultadoProcesamiento(boolean duplicado,
            StockValidationAcceptedEvent acceptedEvent,
            StockRechazadoEvent rechazoEvent) {
        /**
         * Construye el resultado usado cuando el eventId ya habia sido procesado.
         * No incluye eventos salientes porque no debe repetirse la publicacion.
         */
        public static ResultadoProcesamiento eventoDuplicado() {
            return new ResultadoProcesamiento(true, null, null);
        }

        /**
         * Construye el resultado de una compra validada correctamente.
         * Conserva el evento aceptado que sera publicado hacia ventas.
         */
        public static ResultadoProcesamiento procesadoSinRechazo(StockValidationAcceptedEvent acceptedEvent) {
            return new ResultadoProcesamiento(false, acceptedEvent, null);
        }

        /**
         * Construye el resultado de una compra rechazada por reglas de stock.
         * Conserva el evento de rechazo que explica el motivo y sus detalles.
         */
        public static ResultadoProcesamiento conRechazo(StockRechazadoEvent rechazoEvent) {
            return new ResultadoProcesamiento(false, null, rechazoEvent);
        }

        /**
         * Indica si el procesamiento termino con rechazo de stock.
         */
        public boolean tieneRechazo() {
            return rechazoEvent != null;
        }
    }

    /**
     * Resultado de validar un item individual contra el catalogo y su stock.
     *
     * Conserva la pelicula bloqueada cuando existe, la cantidad solicitada, el stock
     * disponible y el tipo de validacion para construir rechazos o descontar stock.
     */
    private record ValidacionItem(PeliculaEntity pelicula,
            Long peliculaId,
            BigDecimal solicitado,
            BigDecimal disponible,
            TipoValidacion tipoValidacion) {

        /**
         * Crea una validacion fallida cuando el item solicita cantidad cero o negativa.
         */
        static ValidacionItem cantidadInvalida(Long peliculaId, BigDecimal solicitado) {
            return new ValidacionItem(null, peliculaId, solicitado, null, TipoValidacion.CANTIDAD_INVALIDA);
        }

        /**
         * Crea una validacion fallida cuando la pelicula no existe o no esta activa.
         */
        static ValidacionItem peliculaInexistente(Long peliculaId, BigDecimal solicitado) {
            return new ValidacionItem(null, peliculaId, solicitado, null, TipoValidacion.PELICULA_INEXISTENTE);
        }

        /**
         * Crea una validacion fallida cuando la pelicula existe pero no alcanza el stock disponible.
         */
        static ValidacionItem stockInsuficiente(PeliculaEntity pelicula, BigDecimal solicitado) {
            return new ValidacionItem(
                    pelicula,
                    pelicula.id(),
                    solicitado,
                    pelicula.stockDisponible(),
                    TipoValidacion.STOCK_INSUFICIENTE);
        }

        /**
         * Crea una validacion exitosa lista para descontar stock sobre la pelicula bloqueada.
         */
        static ValidacionItem ok(PeliculaEntity pelicula, BigDecimal solicitado) {
            return new ValidacionItem(
                    pelicula,
                    pelicula.id(),
                    solicitado,
                    pelicula.stockDisponible(),
                    TipoValidacion.OK);
        }

        /**
         * Indica si la validacion corresponde a un error.
         */
        boolean esError() {
            return tipoValidacion != TipoValidacion.OK;
        }

        /**
         * Indica si la validacion fallo por cantidad no positiva.
         */
        boolean esCantidadInvalida() {
            return tipoValidacion == TipoValidacion.CANTIDAD_INVALIDA;
        }

        /**
         * Indica si la validacion fallo por pelicula inexistente o inactiva.
         */
        boolean esPeliculaInexistente() {
            return tipoValidacion == TipoValidacion.PELICULA_INEXISTENTE;
        }

        /**
         * Indica si la validacion fallo por stock insuficiente.
         */
        boolean esStockInsuficiente() {
            return tipoValidacion == TipoValidacion.STOCK_INSUFICIENTE;
        }

        /**
         * Devuelve el stock disponible como texto para el detalle del rechazo.
         */
        String disponibleComoString() {
            return disponible == null ? null : disponible.stripTrailingZeros().toPlainString();
        }
    }

    /**
     * Clasificacion interna del resultado de validar un item de compra.
     *
     * Diferencia items correctos de fallas por cantidad invalida, pelicula no usable
     * o stock insuficiente, lo que luego define el motivo del rechazo.
     */
    private enum TipoValidacion {
        OK,
        CANTIDAD_INVALIDA,
        PELICULA_INEXISTENTE,
        STOCK_INSUFICIENTE
    }
}
