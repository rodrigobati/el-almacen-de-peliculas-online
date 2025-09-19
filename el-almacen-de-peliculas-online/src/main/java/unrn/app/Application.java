package unrn.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication(scanBasePackages = "unrn")
@EntityScan(basePackages = "unrn.infra.persistence")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
