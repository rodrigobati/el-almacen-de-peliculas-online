package unrn.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Arrays;
import java.util.List;

/**
 * Configuracion central de seguridad HTTP y validacion JWT.
 *
 * Define que endpoints son publicos, cuales requieren rol ADMIN, como se decodifica
 * el token emitido por Keycloak y como se convierten sus roles a authorities de
 * Spring. Tambien lee desde properties los issuers aceptados por ambiente.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri:}")
    private String jwkSetUri;

    @Value("${security.keycloak.client-id:}")
    private String keycloakClientId;

    @Value("${security.keycloak.allowed-issuers:${spring.security.oauth2.resourceserver.jwt.issuer-uri}}")
    private String allowedIssuersProperty;

    /**
     * Define las reglas HTTP de seguridad para endpoints publicos y administrados.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/swagger-ui/", "/v3/api-docs/", "/swagger-ui.html").permitAll()
                        .requestMatchers(HttpMethod.GET, "/categorias").permitAll()
                        .requestMatchers(HttpMethod.GET, "/peliculas", "/peliculas/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/admin/peliculas", "/api/admin/peliculas/**")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/admin/peliculas", "/api/admin/peliculas/**")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/admin/peliculas", "/api/admin/peliculas/**")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/admin/peliculas", "/api/admin/peliculas/**")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/admin/peliculas", "/api/admin/peliculas/**")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/admin/directores", "/api/admin/directores/**")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/admin/directores", "/api/admin/directores/**")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/admin/actores", "/api/admin/actores/**")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/admin/actores", "/api/admin/actores/**")
                        .hasRole("ADMIN")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
        return http.build();
    }

    /**
     * Construye el decodificador JWT y valida emisores permitidos.
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder jwtDecoder;
        if (jwkSetUri != null && !jwkSetUri.isBlank()) {
            jwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
        } else {
            jwtDecoder = JwtDecoders.fromIssuerLocation(issuerUri);
        }

        OAuth2TokenValidator<Jwt> withTimestamp = new JwtTimestampValidator();
        List<String> allowedIssuers = allowedIssuers();
        OAuth2TokenValidator<Jwt> withIssuers = jwt -> {
            String issuer = jwt.getIssuer() == null ? null : jwt.getIssuer().toString();
            if (issuer != null && allowedIssuers.contains(issuer)) {
                return OAuth2TokenValidatorResult.success();
            }
            return OAuth2TokenValidatorResult.failure(
                    new OAuth2Error("invalid_token", "Invalid issuer", null));
        };

        jwtDecoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(withTimestamp, withIssuers));
        return jwtDecoder;
    }

    /**
     * Lee desde properties que emisores de Keycloak puede aceptar el backend.
     */
    private List<String> allowedIssuers() {
        return Arrays.stream(allowedIssuersProperty.split(","))
                .map(String::trim)
                .filter(issuer -> !issuer.isBlank())
                .toList();
    }

    /**
     * Configura la conversion de claims JWT a authorities de Spring Security.
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new JwtRoleConverter(keycloakClientId));
        return converter;
    }
}
