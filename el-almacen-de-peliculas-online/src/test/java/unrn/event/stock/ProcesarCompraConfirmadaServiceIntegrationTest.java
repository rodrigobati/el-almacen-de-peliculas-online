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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = Application.class)
@TestPropertySource(properties = {
        "spring.rabbitmq.listener.simple.auto-startup=false",
        "spring.rabbitmq.listener.direct.auto-startup=false",
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
                new Genero("Ciencia ficción"),
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
        // Setup: evento válido con stock suficiente
        CompraConfirmadaEvent event = new CompraConfirmadaEvent(
                "evento-ok-1",
                9L,
                "cliente-ok",
                java.time.Instant.now(),
                List.of(new CompraConfirmadaEvent.ItemCompraConfirmada(peliculaId, 5)));

        // Ejercitación: procesar una sola vez
        var resultado = procesarCompraConfirmadaService.procesar(event);

        // Verificación: descuento de stock, sin rechazo, evento procesado registrado
        PeliculaEntity peliculaPersistida = entityManager.find(PeliculaEntity.class, peliculaId);

        assertFalse(resultado.duplicado(), "El primer procesamiento no debe ser duplicado");
        assertFalse(resultado.tieneRechazo(), "En camino exitoso no debe existir rechazo");
        assertEquals(new BigDecimal("95.00"), peliculaPersistida.stockDisponible(),
                "El stock debe disminuir según la cantidad comprada");
        assertEquals(1L, eventoProcesadoRepository.count(),
                "Debe registrarse el eventId como procesado");
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

        // Ejercitación: procesar dos veces el mismo evento
        var primerResultado = procesarCompraConfirmadaService.procesar(event);
        var segundoResultado = procesarCompraConfirmadaService.procesar(event);

        // Verificación: un solo descuento y evento procesado único
        PeliculaEntity peliculaPersistida = entityManager.find(PeliculaEntity.class, peliculaId);

        assertFalse(primerResultado.duplicado(), "El primer procesamiento no debe considerarse duplicado");
        assertTrue(segundoResultado.duplicado(), "El segundo procesamiento debe detectarse como duplicado");
        assertEquals(new BigDecimal("90.00"), peliculaPersistida.stockDisponible(),
                "El stock debe disminuir una sola vez en 10 unidades");
        assertEquals(1L, eventoProcesadoRepository.count(),
                "Debe existir un solo registro en eventos_procesados para el eventId");
    }

    @Test
    @DisplayName("ProcesarCompraConfirmada stockInsuficiente generaRechazo y registraEvento")
    void procesarCompraConfirmada_stockInsuficiente_generaRechazoYRegistraEvento() {
        // Setup: solicitar más stock del disponible
        CompraConfirmadaEvent event = new CompraConfirmadaEvent(
                "evento-rechazo-1",
                11L,
                "cliente-2",
                java.time.Instant.now(),
                List.of(new CompraConfirmadaEvent.ItemCompraConfirmada(peliculaId, 1000)));

        // Ejercitación: procesar compra insuficiente
        var resultado = procesarCompraConfirmadaService.procesar(event);

        // Verificación: rechazo informado y evento marcado como procesado
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
    }
}
