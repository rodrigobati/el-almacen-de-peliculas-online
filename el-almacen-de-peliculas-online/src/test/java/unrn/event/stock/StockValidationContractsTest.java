package unrn.event.stock;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de contrato de los eventos del flujo de validacion de stock.
 *
 * Verifican que los records de solicitud, aceptacion y rechazo mantengan estructura,
 * valores y detalles compatibles con los mensajes intercambiados con ventas.
 */
class StockValidationContractsTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    @DisplayName("StockValidationRequestedEvent deserializacionDesdeJsonCompatiblePreservaCampos")
    void stockValidationRequestedEvent_deserializacionDesdeJsonCompatiblePreservaCampos() throws Exception {
        // Setup: JSON esperado desde ventas
        String json = """
                {
                  "eventId": "evt-req-2",
                  "compraId": 99,
                  "items": [
                    {
                      "peliculaId": 12,
                      "cantidad": 4
                    }
                  ],
                  "occurredAt": "2026-06-09T12:00:00Z"
                }
                """;

        // EjercitaciÃ³n: deserializar en catalogo
        StockValidationRequestedEvent event = objectMapper.readValue(json, StockValidationRequestedEvent.class);

        // VerificaciÃ³n: correlacion, compra y shape minimo de items
        assertEquals("evt-req-2", event.eventId(), "eventId debe preservarse");
        assertEquals(99L, event.compraId(), "compraId debe preservarse");
        assertEquals(1, event.items().size(), "Debe conservar items");
        assertEquals(12L, event.items().get(0).peliculaId(), "peliculaId debe preservarse");
        assertEquals(4, event.items().get(0).cantidad(), "cantidad debe preservarse");
        assertEquals(Instant.parse("2026-06-09T12:00:00Z"), event.occurredAt(), "occurredAt debe preservarse");
    }

    @Test
    @DisplayName("StockValidationAcceptedEvent serializacionMantieneContratoEsperado")
    void stockValidationAcceptedEvent_serializacionMantieneContratoEsperado() throws Exception {
        // Setup: evento accepted publicado por catalogo
        StockValidationAcceptedEvent event = new StockValidationAcceptedEvent(
                "evt-acc-2",
                100L,
                Instant.parse("2026-06-09T13:00:00Z"));

        // EjercitaciÃ³n: serializar a JSON
        String json = objectMapper.writeValueAsString(event);
        JsonNode root = objectMapper.readTree(json);

        // VerificaciÃ³n: contrato de accepted
        assertTrue(root.has("eventId"), "El contrato accepted debe incluir eventId");
        assertTrue(root.has("compraId"), "El contrato accepted debe incluir compraId");
        assertTrue(root.has("occurredAt"), "El contrato accepted debe incluir occurredAt");
        assertEquals("evt-acc-2", root.get("eventId").asText(), "eventId debe preservarse");
        assertEquals(100L, root.get("compraId").asLong(), "compraId debe preservarse");
    }

    @Test
    @DisplayName("StockRechazadoEvent serializacionMantieneContratoEsperado")
    void stockRechazadoEvent_serializacionMantieneContratoEsperado() throws Exception {
        // Setup: evento rechazado publicado por catalogo
        StockRechazadoEvent event = new StockRechazadoEvent(
                "evt-rej-2",
                101L,
                "STOCK_INSUFICIENTE",
                List.of(new StockRechazadoEvent.DetalleStockRechazado(20L, 5, "1")));

        // EjercitaciÃ³n: serializar a JSON
        String json = objectMapper.writeValueAsString(event);
        JsonNode root = objectMapper.readTree(json);
        JsonNode detalle = root.get("detalles").get(0);

        // VerificaciÃ³n: contrato de rechazo compatible con ventas
        assertTrue(root.has("eventId"), "El contrato rechazado debe incluir eventId");
        assertTrue(root.has("compraId"), "El contrato rechazado debe incluir compraId");
        assertTrue(root.has("motivo"), "El contrato rechazado debe incluir motivo");
        assertTrue(root.has("detalles"), "El contrato rechazado debe incluir detalles");
        assertEquals("evt-rej-2", root.get("eventId").asText(), "eventId debe preservarse");
        assertEquals(101L, root.get("compraId").asLong(), "compraId debe preservarse");

        assertTrue(detalle.has("peliculaId"), "Cada detalle debe incluir peliculaId");
        assertTrue(detalle.has("solicitado"), "Cada detalle debe incluir solicitado");
        assertTrue(detalle.has("disponible"), "Cada detalle debe incluir disponible");
        assertFalse(detalle.has("titulo"), "El detalle no debe incluir titulo");
        assertFalse(detalle.has("precio"), "El detalle no debe incluir precio");
    }
}
