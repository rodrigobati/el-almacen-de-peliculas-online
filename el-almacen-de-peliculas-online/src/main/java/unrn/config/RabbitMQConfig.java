package unrn.config;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class RabbitMQConfig {

    private static final Logger logger = LoggerFactory.getLogger(RabbitMQConfig.class);

    @Bean
    public Connection rabbitConnection(
            @Value("${rabbitmq.host:localhost}") String host,
            @Value("${rabbitmq.port:5672}") int port,
            @Value("${rabbitmq.username:guest}") String username,
            @Value("${rabbitmq.password:guest}") String password) {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        factory.setUsername(username);
        factory.setPassword(password);

        try {
            return factory.newConnection();
        } catch (Exception e) {
            logger.warn("No se pudo conectar a RabbitMQ en {}:{} - consumidor deshabilitado: {}", host, port, e.getMessage());
            return null;
        }
    }
}