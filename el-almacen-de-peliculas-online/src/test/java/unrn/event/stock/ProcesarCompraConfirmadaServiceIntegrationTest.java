package unrn.event.stock;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import unrn.app.Application;
import unrn.infra.persistence.EventoProcesadoRepository;
import unrn.infra.persistence.PeliculaEntity;
import unrn.infra.persistence.PeliculaRepository;
import unrn.model.Actor;
import unrn.model.Condicion;
import unrn.model.Director;
import unrn.model.Formato;
import unrn.model.Genero;
import unrn.model.Pelicula;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de integracion del procesamiento transaccional de compras y stock.
 *
 * Cubren idempotencia por eventId, validacion de cantidades, peliculas inexistentes,
 * stock insuficiente, descuento con lock y registro de eventos aceptados o rechazados
 * en el outbox.
 */
@SpringBootTest(classes = Application.class)
@TestPropertySource(properties = {
        "spring.rabbitmq.listener.simple.auto-startup=false",
        "spring.rabbitmq.listener.direct.auto-startup=false",
        "catalogo.outbox.scheduler.enabled=false",
        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost/mock-jwks"
})
class ProcesarCompraConfirmadaServiceIntegrationTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PeliculaRepository peliculaRepository;

    @Autowired
    private EventoProcesadoRepository eventoProcesadoRepository;

    @Autowired
    private ProcesarCompraConfirmadaService procesarCompraConfirmadaService;

    private Long peliculaId;

    @BeforeEach
    void beforeEach() {
        jdbcTemplate.execute("DELETE FROM catalogo_outbox_event");
        jdbcTemplate.execute("DELETE FROM eventos_procesados");
        jdbcTemplate.execute("DELETE FROM pelicula_actor");
        jdbcTemplate.execute("DELETE FROM pelicula_director");
        jdbcTemplate.execute("DELETE FROM pelicula");
        jdbcTemplate.execute("DELETE FROM actor");
        jdbcTemplate.execute("DELETE FROM director");
        jdbcTemplate.execute("DELETE FROM condicion");
        jdbcTemplate.execute("DELETE FROM formato");
        jdbcTemplate.execute("DELETE FROM genero");

        Pelicula pelicula = new Pelicula(
                "Matrix",
                new Condicion("nuevo"),
                List.of(new Director("Lana Wachowski")),
                100.00,
                new Formato("BluRay"),
                new Genero("Ciencia ficciÃ³n"),
                "Sinopsis",
                List.of(new Actor("Keanu Reeves")),
                "",
                LocalDate.of(1999, 3, 31),
                5);

        peliculaId = peliculaRepository.guardar(pelicula).id();
    }

    @Test
    @DisplayName("ProcesarCompraConfirmada caminoExitoso descuentaStock y noGeneraRechazo")
    void procesarCompraConfirmada_caminoExitoso_descuentaStockYNoGeneraRechazo() {
        // Setup: evento vÃ¡lido con stock suficiente
        CompraConfirmadaEvent event = new CompraConfirmadaEvent(
                "evento-ok-1",
                9L,
                "cliente-ok",
                java.time.Instant.now(),
                List.of(new CompraConfirmadaEvent.ItemCompraConfirmada(peliculaId, 5)));

        // EjercitaciÃ³n: procesar una sola vez
        var resultado = procesarCompraConfirmadaService.procesar(event);

        // VerificaciÃ³n: descuento de stock, sin rechazo, evento procesado registrado
        PeliculaEntity peliculaPersistida = entityManager.find(PeliculaEntity.class, peliculaId);

        assertFalse(resultado.duplicado(), "El primer procesamiento no debe ser duplicado");
        assertFalse(resultado.tieneRechazo(), "En camino exitoso no debe existir rechazo");
        assertEquals(new BigDecimal("95.00"), peliculaPersistida.stockDisponible(),
                "El stock debe disminuir segÃºn la cantidad comprada");
        assertEquals(1L, eventoProcesadoRepository.count(),
                "Debe registrarse el eventId como procesado");
        assertEquals(1, contarOutboxPorTipo("StockValidationAcceptedEvent"),
                "Debe registrarse el accepted en outbox");
    }

    @Test
    @DisplayName("ProcesarCompraConfirmada eventoDuplicado descuentaStockUnaSolaVez")
    void procesarCompraConfirmada_eventoDuplicado_descuentaStockUnaSolaVez() {
        // Setup: evento de compra con id fijo
        CompraConfirmadaEvent event = new CompraConfirmadaEvent(
                "evento-duplicado-1",
                10L,
                "cliente-1",
                java.time.Instant.now(),
                List.of(new CompraConfirmadaEvent.ItemCompraConfirmada(peliculaId, 10)));

        // EjercitaciÃ³n: procesar dos veces el mismo evento
        var primerResultado = procesarCompraConfirmadaService.procesar(event);
        var segundoResultado = procesarCompraConfirmadaService.procesar(event);

        // VerificaciÃ³n: un solo descuento y evento procesado Ãºnico
        PeliculaEntity peliculaPersistida = entityManager.find(PeliculaEntity.class, peliculaId);

        assertFalse(primerResultado.duplicado(), "El primer procesamiento no debe considerarse duplicado");
        assertTrue(segundoResultado.duplicado(), "El segundo procesamiento debe detectarse como duplicado");
        assertEquals(new BigDecimal("90.00"), peliculaPersistida.stockDisponible(),
                "El stock debe disminuir una sola vez en 10 unidades");
        assertEquals(1L, eventoProcesadoRepository.count(),
                "Debe existir un solo registro en eventos_procesados para el eventId");
        assertEquals(1, contarOutboxPorTipo("StockValidationAcceptedEvent"),
                "El duplicado no debe generar otro accepted en outbox");
    }

    @Test
    @DisplayName("ProcesarCompraConfirmada stockInsuficiente generaRechazo y registraEvento")
    void procesarCompraConfirmada_stockInsuficiente_generaRechazoYRegistraEvento() {
        // Setup: solicitar mÃ¡s stock del disponible
        CompraConfirmadaEvent event = new CompraConfirmadaEvent(
                "evento-rechazo-1",
                11L,
                "cliente-2",
                java.time.Instant.now(),
                List.of(new CompraConfirmadaEvent.ItemCompraConfirmada(peliculaId, 1000)));

        // EjercitaciÃ³n: procesar compra insuficiente
        var resultado = procesarCompraConfirmadaService.procesar(event);

        // VerificaciÃ³n: rechazo informado y evento marcado como procesado
        assertFalse(resultado.duplicado(), "No debe marcarse duplicado en el primer intento");
        assertTrue(resultado.tieneRechazo(), "Debe devolver evento de stock rechazado");
        assertNotNull(resultado.rechazoEvent(), "El evento de rechazo no debe ser nulo");
        assertEquals(ProcesarCompraConfirmadaService.MOTIVO_STOCK_INSUFICIENTE, resultado.rechazoEvent().motivo(),
                "El motivo debe indicar stock insuficiente");
        assertEquals(1L, eventoProcesadoRepository.count(),
                "El evento rechazado debe marcarse como procesado para idempotencia");

        PeliculaEntity peliculaPersistida = entityManager.find(PeliculaEntity.class, peliculaId);
        assertEquals(new BigDecimal("100.00"), peliculaPersistida.stockDisponible(),
                "Ante rechazo no se debe descontar stock");
        assertEquals(1, contarOutboxPorTipo("StockRechazadoEvent"),
                "Debe registrarse el rechazo en outbox");
    }

    @Test
    @DisplayName("ProcesarCompraConfirmada cantidadInvalida generaRechazo y noModificaStock")
    void procesarCompraConfirmada_cantidadInvalida_generaRechazoYNoModificaStock() {
        // Setup: cantidad negativa no debe aumentar stock
        CompraConfirmadaEvent event = new CompraConfirmadaEvent(
                "evento-cantidad-invalida-1",
                14L,
                "cliente-cantidad-invalida",
                java.time.Instant.now(),
                List.of(new CompraConfirmadaEvent.ItemCompraConfirmada(peliculaId, -5)));

        // Ejercitacion: procesar compra con cantidad invalida
        var resultado = procesarCompraConfirmadaService.procesar(event);

        // Verificacion: rechazo informado, stock sin cambios y evento marcado como procesado
        assertFalse(resultado.duplicado(), "No debe marcarse duplicado en el primer intento");
        assertTrue(resultado.tieneRechazo(), "Debe devolver evento de stock rechazado");
        assertNotNull(resultado.rechazoEvent(), "El evento de rechazo no debe ser nulo");
        assertEquals(ProcesarCompraConfirmadaService.MOTIVO_CANTIDAD_INVALIDA, resultado.rechazoEvent().motivo(),
                "El motivo debe indicar cantidad invalida");
        assertEquals(1, resultado.rechazoEvent().detalles().size(),
                "Debe informar el item invalido en el detalle de rechazo");
        assertEquals(-5, resultado.rechazoEvent().detalles().get(0).solicitado(),
                "El detalle debe preservar la cantidad solicitada invalida");
        assertEquals(1L, eventoProcesadoRepository.count(),
                "El evento rechazado debe marcarse como procesado para idempotencia");

        PeliculaEntity peliculaPersistida = entityManager.find(PeliculaEntity.class, peliculaId);
        assertEquals(new BigDecimal("100.00"), peliculaPersistida.stockDisponible(),
                "Ante cantidad invalida no se debe modificar stock");
        assertEquals(1, contarOutboxPorTipo("StockRechazadoEvent"),
                "Debe registrarse el rechazo en outbox");
    }

        @Test
        @DisplayName("ProcesarCompraConfirmada solicitudesConcurrentes evitaSobreventa con bloqueoPesimista")
        void procesarCompraConfirmada_solicitudesConcurrentes_evitaSobreventaConBloqueoPesimista() throws Exception {
                // Setup: dos eventos distintos compiten por el mismo stock disponible
                CompraConfirmadaEvent primerEvento = new CompraConfirmadaEvent(
                                "evento-concurrente-1",
                                12L,
                                "cliente-a",
                                java.time.Instant.now(),
                                List.of(new CompraConfirmadaEvent.ItemCompraConfirmada(peliculaId, 70)));

                CompraConfirmadaEvent segundoEvento = new CompraConfirmadaEvent(
                                "evento-concurrente-2",
                                13L,
                                "cliente-b",
                                java.time.Instant.now(),
                                List.of(new CompraConfirmadaEvent.ItemCompraConfirmada(peliculaId, 70)));

                ExecutorService executor = Executors.newFixedThreadPool(2);
                CountDownLatch ready = new CountDownLatch(2);
                CountDownLatch start = new CountDownLatch(1);

                try {
                        Future<ProcesarCompraConfirmadaService.ResultadoProcesamiento> primerResultadoFuture = executor.submit(() -> {
                                ready.countDown();
                                start.await(5, TimeUnit.SECONDS);
                                return procesarCompraConfirmadaService.procesar(primerEvento);
                        });

                        Future<ProcesarCompraConfirmadaService.ResultadoProcesamiento> segundoResultadoFuture = executor.submit(() -> {
                                ready.countDown();
                                start.await(5, TimeUnit.SECONDS);
                                return procesarCompraConfirmadaService.procesar(segundoEvento);
                        });

                        ready.await(5, TimeUnit.SECONDS);
                        start.countDown();

                        var primerResultado = primerResultadoFuture.get(10, TimeUnit.SECONDS);
                        var segundoResultado = segundoResultadoFuture.get(10, TimeUnit.SECONDS);

                        // VerificaciÃ³n: no hay sobreventa y solo un procesamiento descuenta stock
                        int exitos = 0;
                        int rechazos = 0;

                        for (var resultado : List.of(primerResultado, segundoResultado)) {
                                if (resultado.tieneRechazo()) {
                                        rechazos++;
                                } else if (!resultado.duplicado()) {
                                        exitos++;
                                }
                        }

                        PeliculaEntity peliculaPersistida = entityManager.find(PeliculaEntity.class, peliculaId);

                        assertEquals(1, exitos,
                                        "Solo una solicitud concurrente debe poder descontar stock");
                        assertEquals(1, rechazos,
                                        "La otra solicitud concurrente debe rechazarse por stock insuficiente");
                        assertEquals(new BigDecimal("30.00"), peliculaPersistida.stockDisponible(),
                                        "El stock final debe ser consistente y nunca negativo");
                        assertEquals(2L, eventoProcesadoRepository.count(),
                                        "Cada eventId distinto debe registrarse una sola vez");
                        assertEquals(2, contarOutboxPendientes(),
                                        "Cada resultado concurrente debe quedar registrado en outbox");
                } finally {
                        executor.shutdownNow();
                }
        }

        private int contarOutboxPorTipo(String eventType) {
                return jdbcTemplate.queryForObject(
                                "SELECT COUNT(*) FROM catalogo_outbox_event WHERE event_type=? AND status='PENDING'",
                                Integer.class,
                                eventType);
        }

        private int contarOutboxPendientes() {
                return jdbcTemplate.queryForObject(
                                "SELECT COUNT(*) FROM catalogo_outbox_event WHERE status='PENDING'",
                                Integer.class);
        }
}
