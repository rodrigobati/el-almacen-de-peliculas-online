# Security Authorization Verification Report

Date: 2026-02-11
Scope: Keycloak, OAuth2 Resource Server, admin authorization for PeliculaAdminController

## 1) Active Endpoint Selected

- Probed endpoints:
  - http://localhost:8081/api/admin/peliculas -> HTTP 500
  - http://localhost:8080/api/admin/peliculas -> HTTP 000 (no response)
- Selected active endpoint: http://localhost:8081/api/admin/peliculas
- Reason: 8081 returned an HTTP response while 8080 did not.

## 2) Token Acquisition Result

- Keycloak token endpoint: http://localhost:9090/realms/videoclub/protocol/openid-connect/token
- Client: web (public, no secret)
- User: usuarioadmin / usuarioadmin
- Result: SUCCESS (access_token obtained)

## 3) JWT Role Evidence (decoded section only)

{
"realm_access": {
"roles": [
"offline_access",
"ROLE_ADMIN",
"uma_authorization",
"default-roles-videoclub"
]
}
}

## 4) Endpoint Tests

- No token -> HTTP 500
- With admin token (usuarioadmin) -> HTTP 500
- Non-admin token test: NOT AVAILABLE
  - Attempted user: usuariocliente / usuariocliente
  - Keycloak response: invalid_grant (Account is not fully set up)

## 5) Final Verdict

- Authorization is not effectively enforced for /api/admin/peliculas.
- The backend currently allows all requests (security is disabled), so admin endpoints are not protected.
- The 500 response is unrelated to authorization and indicates an application error on the endpoint.

Verdict: NOT PROTECTED

## 6) Required Remediation (Exact Fix)

Replace the permissive security config with a JWT Resource Server configuration that:

- Extracts realm roles from realm_access.roles
- Restricts /api/admin/\*\* to ROLE_ADMIN

Target file:

- el-almacen-de-peliculas-online/src/main/java/unrn/config/SecurityConfig.java

Recommended replacement:

```java
package unrn.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
        return http.build();
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
```

## 7) Post-Fix Expected Results

- No token -> 401 or 403
- With admin token -> 200
- With non-admin token -> 403

Notes:

- Keep Keycloak role name as ROLE_ADMIN; no change required on Keycloak.
- Investigate the 500 error separately after authorization is enforced.
