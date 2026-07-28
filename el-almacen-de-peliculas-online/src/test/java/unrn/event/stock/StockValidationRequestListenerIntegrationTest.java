package unrn.event.stock;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import unrn.app.Application;
import unrn.infra.persistence.PeliculaEntity;
import unrn.infra.persistence.PeliculaRepository;
import unrn.model.Actor;
import unrn.model.Condicion;
import unrn.model.Director;
import unrn.model.Formato;
import unrn.model.Genero;
import unrn.model.Pelicula;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Pruebas de integracion del listener de solicitudes de validacion de stock.
 *
 * Aseguran que el mensaje recibido desde RabbitMQ se adapte al modelo interno de
 * compra y delegue correctamente el procesamiento transaccional del stock.
 */
@SpringBootTest(classes = Application.class)
@TestPropertySource(properties = {
        "spring.rabbitmq.listener.simple.auto-startup=false",
        "spring.rabbitmq.listener.direct.auto-startup=false",
        "catalogo.outbox.scheduler.enabled=false",
        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost/mock-jwks"
})
class StockValidationRequestListenerIntegrationTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PeliculaRepository peliculaRepository;

    @Autowired
    private StockValidationRequestListener listener;

    @MockBean
    private StockRechazadoPublisher stockRechazadoPublisher;

    @MockBean
    private StockValidationAcceptedPublisher stockValidationAcceptedPublisher;

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
                new Genero("Ciencia ficcion"),
                "Sinopsis",
                List.of(new Actor("Keanu Reeves")),
                "",
                LocalDate.of(1999, 3, 31),
                5);

        peliculaId = peliculaRepository.guardar(pelicula).id();
    }

    @Test
    @DisplayName("StockValidationRequestListener solicitudValida descuentaStock y registraAcceptedEnOutbox")
    void stockValidationRequestListener_solicitudValida_descuentaStockYRegistraAcceptedEnOutbox() {
        StockValidationRequestedEvent event = new StockValidationRequestedEvent(
                "stock-req-ok-1",
                100L,
                List.of(new StockValidationRequestedEvent.Item(peliculaId, 5)),
                Instant.now());

        listener.onStockValidationRequested(event);

        verify(stockValidationAcceptedPublisher, never()).publicarAfterCommit(any(StockValidationAcceptedEvent.class));
        verify(stockRechazadoPublisher, never()).publicarAfterCommit(any(StockRechazadoEvent.class));

        PeliculaEntity peliculaPersistida = entityManager.find(PeliculaEntity.class, peliculaId);
        assertEquals(new BigDecimal("95.00"), peliculaPersistida.stockDisponible(),
                "Debe descontar stock cuando la validacion es aceptada");

        Integer outboxAccepted = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM catalogo_outbox_event WHERE event_type='StockValidationAcceptedEvent' AND status='PENDING'",
                Integer.class);
        assertEquals(1, outboxAccepted,
                "Debe registrar un StockValidationAcceptedEvent pendiente en outbox");
    }

    @Test
    @DisplayName("StockValidationRequestListener stockInsuficiente noDescuentaStock y registraRechazoEnOutbox")
    void stockValidationRequestListener_stockInsuficiente_noDescuentaStockYRegistraRechazoEnOutbox() {
        StockValidationRequestedEvent event = new StockValidationRequestedEvent(
                "stock-req-rej-1",
                101L,
                List.of(new StockValidationRequestedEvent.Item(peliculaId, 1000)),
                Instant.now());

        listener.onStockValidationRequested(event);

        verify(stockRechazadoPublisher, never()).publicarAfterCommit(any(StockRechazadoEvent.class));
        verify(stockValidationAcceptedPublisher, never()).publicarAfterCommit(any(StockValidationAcceptedEvent.class));

        PeliculaEntity peliculaPersistida = entityManager.find(PeliculaEntity.class, peliculaId);
        assertEquals(new BigDecimal("100.00"), peliculaPersistida.stockDisponible(),
                "No debe descontar stock cuando la validacion es rechazada");

        Integer outboxRechazado = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM catalogo_outbox_event WHERE event_type='StockRechazadoEvent' AND status='PENDING'",
                Integer.class);
        assertEquals(1, outboxRechazado,
                "Debe registrar un StockRechazadoEvent pendiente en outbox");
    }
}
