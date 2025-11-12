package unrn.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // desactiva CSRF para APIs REST

                .authorizeHttpRequests(auth -> auth
                        // endpoints de administración (rol ROLE_ADMIN de Keycloak)
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // todo lo demás público
                        .anyRequest().permitAll())

                // habilita autenticación via JWT (Keycloak)
                .oauth2ResourceServer(oauth2 -> oauth2.jwt());

        return http.build();
    }
}