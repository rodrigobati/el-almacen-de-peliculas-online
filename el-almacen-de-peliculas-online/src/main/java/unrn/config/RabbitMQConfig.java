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

@Configuration
public class RabbitMQConfig {

    static final String VENTAS_EVENTS_EXCHANGE = "ventas.events";
    static final String CATALOGO_EVENTS_EXCHANGE = "catalogo.events";
    static final String CATALOGO_COMPRA_CONFIRMADA_QUEUE = "catalogo.q.ventas-compra-confirmada";
    static final String VENTAS_COMPRA_CONFIRMADA_ROUTING_KEY = "ventas.compra.confirmada";

    // Event
    @Value("${rabbitmq.event.exchange.name}")
    private String eventExchange;

    @Bean
    public TopicExchange exchangeVideoCloub00() {
        return new TopicExchange(eventExchange);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public TopicExchange ventasEventsExchange() {
        return new TopicExchange(VENTAS_EVENTS_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange catalogoEventsExchange() {
        return new TopicExchange(CATALOGO_EVENTS_EXCHANGE, true, false);
    }

    @Bean
    public Queue catalogoCompraConfirmadaQueue() {
        return new Queue(CATALOGO_COMPRA_CONFIRMADA_QUEUE, true);
    }

    @Bean
    public Binding catalogoCompraConfirmadaBinding() {
        return BindingBuilder
                .bind(catalogoCompraConfirmadaQueue())
                .to(ventasEventsExchange())
                .with(VENTAS_COMPRA_CONFIRMADA_ROUTING_KEY);
    }
}