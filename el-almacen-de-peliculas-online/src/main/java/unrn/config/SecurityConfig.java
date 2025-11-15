package unrn.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
                .oauth2ResourceServer(
                        oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));

        return http.build();
    }

    /**
     * Convierte los roles provistos por Keycloak (en el claim `realm_access.roles`)
     * a GrantedAuthorities para que `hasRole("ADMIN")` funcione.
     */
    private Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<GrantedAuthority> authorities = new ArrayList<>();

            // extrae roles de realm_access.roles (Keycloak)
            var realmAccess = jwt.getClaimAsMap("realm_access");
            if (realmAccess != null && realmAccess.containsKey("roles")) {
                Object rolesObj = realmAccess.get("roles");
                if (rolesObj instanceof List) {
                    List<?> rolesList = (List<?>) rolesObj;
                    for (Object r : rolesList) {
                        if (r != null) {
                            String role = r.toString();
                            // si ya viene con ROLE_ no lo volvemos a prefijar
                            if (!role.startsWith("ROLE_")) {
                                role = "ROLE_" + role;
                            }
                            authorities.add(new SimpleGrantedAuthority(role));
                        }
                    }
                }
            }

            // también incluir scopes/authorities estándar si presentes
            JwtGrantedAuthoritiesConverter defaultConverter = new JwtGrantedAuthoritiesConverter();
            authorities.addAll(defaultConverter.convert(jwt));

            return authorities;
        });

        return converter;
    }
}
