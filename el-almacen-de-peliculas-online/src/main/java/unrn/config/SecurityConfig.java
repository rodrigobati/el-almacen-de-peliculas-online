package unrn.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri:}")
    private String jwkSetUri;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health").permitAll()
                    .requestMatchers(HttpMethod.GET, "/categorias").permitAll()
                    .requestMatchers(HttpMethod.GET, "/peliculas", "/peliculas/**").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/admin/peliculas", "/api/admin/peliculas/**")
                    .hasAuthority("ROLE_ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/admin/peliculas", "/api/admin/peliculas/**")
                    .hasAuthority("ROLE_ADMIN")
                    .requestMatchers(HttpMethod.PATCH, "/api/admin/peliculas", "/api/admin/peliculas/**")
                    .hasAuthority("ROLE_ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/admin/peliculas", "/api/admin/peliculas/**")
                    .hasAuthority("ROLE_ADMIN")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder jwtDecoder;
        if (jwkSetUri != null && !jwkSetUri.isBlank()) {
            jwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
        } else {
            jwtDecoder = JwtDecoders.fromIssuerLocation(issuerUri);
        }

        OAuth2TokenValidator<Jwt> withTimestamp = new JwtTimestampValidator();
        List<String> allowedIssuers = List.of(
                "http://keycloak-sso:8080/realms/videoclub",
                "http://localhost:9090/realms/videoclub");
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

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakRealmRoleConverter());
        return converter;
    }

    static class KeycloakRealmRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
        @Override
        public Collection<GrantedAuthority> convert(Jwt jwt) {
            Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
            List<String> roles = realmAccess == null ? List.of() : (List<String>) realmAccess.get("roles");
            return roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
        }
    }
}
