package unrn.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuracion de infraestructura RabbitMQ de la vertical catalogo.
 *
 * Declara exchanges, colas, bindings y conversor JSON que conectan esta aplicacion
 * con eventos de ventas, stock y catalogo. Centraliza nombres configurables para
 * que listeners y publishers no construyan la topologia por su cuenta.
 */
@Configuration
public class RabbitMQConfig {

    static final String VENTAS_EVENTS_EXCHANGE = "ventas.events";
    static final String CATALOGO_EVENTS_EXCHANGE = "catalogo.events";
    static final String CATALOGO_COMPRA_CONFIRMADA_QUEUE = "catalogo.q.ventas-compra-confirmada";
    static final String CATALOGO_STOCK_VALIDATION_REQUESTS_QUEUE = "catalogo.stock.validation.requests";
    static final String VENTAS_COMPRA_CONFIRMADA_ROUTING_KEY = "ventas.compra.confirmada";
    static final String CATALOGO_STOCK_VALIDATION_REQUESTED_ROUTING_KEY = "catalogo.stock.validation.requested";

    // Event
    @Value("${rabbitmq.event.exchange.name}")
    private String eventExchange;

    @Value("${rabbitmq.ventas.events.exchange:" + VENTAS_EVENTS_EXCHANGE + "}")
    private String ventasEventsExchange;

    @Value("${rabbitmq.catalogo.stock.validation.requested.queue:"
            + CATALOGO_STOCK_VALIDATION_REQUESTS_QUEUE + "}")
    private String catalogoStockValidationRequestedQueue;

    @Value("${rabbitmq.catalogo.stock.validation.requested.routing-key:"
            + CATALOGO_STOCK_VALIDATION_REQUESTED_ROUTING_KEY + "}")
    private String catalogoStockValidationRequestedRoutingKey;

    /**
     * Declara el exchange topic legado usado por eventos de pelicula y rating.
     * El nombre sale de rabbitmq.event.exchange.name para que el ambiente decida la topologia.
     */
    @Bean
    public TopicExchange exchangeVideoCloub00() {
        return new TopicExchange(eventExchange);
    }

    /**
     * Crea el conversor JSON usado por RabbitMQ para serializar mensajes.
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    /**
     * Declara el exchange topic donde ventas publica eventos que catalogo debe consumir.
     */
    @Bean
    public TopicExchange ventasEventsExchange() {
        return new TopicExchange(ventasEventsExchange, true, false);
    }

    /**
     * Declara el exchange topic propio de catalogo para eventos salientes de esta vertical.
     */
    @Bean
    public TopicExchange catalogoEventsExchange() {
        return new TopicExchange(CATALOGO_EVENTS_EXCHANGE, true, false);
    }

    /**
     * Declara la cola legado donde catalogo recibe compras confirmadas desde ventas.
     */
    @Bean
    public Queue catalogoCompraConfirmadaQueue() {
        return new Queue(CATALOGO_COMPRA_CONFIRMADA_QUEUE, true);
    }

    /**
     * Vincula la cola legado de compras confirmadas al exchange de ventas con su routing key.
     */
    @Bean
    public Binding catalogoCompraConfirmadaBinding() {
        return BindingBuilder
                .bind(catalogoCompraConfirmadaQueue())
                .to(ventasEventsExchange())
                .with(VENTAS_COMPRA_CONFIRMADA_ROUTING_KEY);
    }

    /**
     * Declara la cola donde catalogo recibe solicitudes de validacion de stock.
     */
    @Bean
    public Queue catalogoStockValidationRequestsQueue() {
        return new Queue(catalogoStockValidationRequestedQueue, true);
    }

    /**
     * Vincula la cola de validacion de stock al exchange de ventas con la routing key configurada.
     */
    @Bean
    public Binding catalogoStockValidationRequestsBinding() {
        return BindingBuilder
                .bind(catalogoStockValidationRequestsQueue())
                .to(ventasEventsExchange())
                .with(catalogoStockValidationRequestedRoutingKey);
    }
}
