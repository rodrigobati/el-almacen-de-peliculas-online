package unrn.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Pruebas de contrato para la topologia RabbitMQ del flujo de stock.
 *
 * Protegen nombres de exchanges, colas y routing keys usados para recibir
 * solicitudes de validacion de stock desde ventas y evitar desalineaciones de
 * integracion entre verticales.
 */
class RabbitMqStockValidationRoutingContractTest {

    @Test
    @DisplayName("RoutingStockValidation configuracionCatalogoMantieneContratoConVentas")
    void routingStockValidation_configuracionCatalogoMantieneContratoConVentas() throws Exception {
        // Setup: leer propiedades productivas del modulo catalogo
        Properties properties = new Properties();
                Path mainPropertiesPath = Path.of("src", "main", "resources", "application.properties");
                try (InputStream input = Files.newInputStream(mainPropertiesPath)) {
            assertNotNull(input, "Debe existir application.properties en classpath");
            properties.load(input);
        }

        // EjercitaciÃ³n y VerificaciÃ³n: request flow ventas -> catalogo
        assertEquals(RabbitMQConfig.VENTAS_EVENTS_EXCHANGE,
                properties.getProperty("rabbitmq.ventas.events.exchange"),
                "El exchange request definido en catalogo debe coincidir con el contrato de ventas");
        assertEquals(RabbitMQConfig.CATALOGO_STOCK_VALIDATION_REQUESTS_QUEUE,
                properties.getProperty("rabbitmq.catalogo.stock.validation.requested.queue",
                        RabbitMQConfig.CATALOGO_STOCK_VALIDATION_REQUESTS_QUEUE),
                "La queue request definida en catalogo debe coincidir con el contrato");
        assertEquals(RabbitMQConfig.CATALOGO_STOCK_VALIDATION_REQUESTED_ROUTING_KEY,
                properties.getProperty("rabbitmq.catalogo.stock.validation.requested.routing-key",
                        RabbitMQConfig.CATALOGO_STOCK_VALIDATION_REQUESTED_ROUTING_KEY),
                "El routing key request definido en catalogo debe coincidir con el contrato");

        // VerificaciÃ³n: result flow catalogo -> ventas sobre exchange compartido
        assertEquals("catalogo.events", properties.getProperty("rabbitmq.catalogo.events.exchange"),
                "El exchange de resultados debe coincidir con el contrato");
        assertEquals("catalogo.stock.validation.accepted",
                properties.getProperty("rabbitmq.catalogo.stock.validation.accepted.routing-key"),
                "El routing key accepted debe coincidir con el contrato");
        assertEquals("catalogo.stock.rechazado",
                properties.getProperty("rabbitmq.catalogo.stock.rechazado.routing-key"),
                "El routing key rechazado debe coincidir con el contrato");
    }
}
