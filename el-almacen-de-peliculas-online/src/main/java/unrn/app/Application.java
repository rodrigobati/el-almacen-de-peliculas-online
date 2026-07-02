package unrn.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Clase principal de arranque Spring Boot para la vertical de catalogo.
 *
 * Configura el escaneo de componentes bajo unrn, registra entidades JPA de
 * persistencia y habilita tareas programadas como el publicador de outbox. Es el
 * punto de entrada que se usa para levantar la API y los listeners de la aplicacion.
 */
@SpringBootApplication(scanBasePackages = "unrn")
@EntityScan(basePackages = "unrn.infra.persistence")
@EnableScheduling
public class Application {
    /**
     * Punto de entrada para ejecutar esta aplicacion.
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
