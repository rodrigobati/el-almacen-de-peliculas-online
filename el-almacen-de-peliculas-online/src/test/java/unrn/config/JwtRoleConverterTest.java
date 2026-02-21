package unrn.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtRoleConverterTest {

    @Test
    @DisplayName("JwtRoleConverter emits canonical role")
    void JwtRoleConverter_emitsCanonicalRole() {
        // Setup: Preparar el escenario
        JwtRoleConverter converter = new JwtRoleConverter();
        Jwt jwt = jwtWithRoles(
                List.of("admin", "ROLE_ADMIN"),
                Map.of("web", Map.of("roles", List.of("admin", "client"))));

        // Ejercitación: Ejecutar la acción a probar
        Collection<GrantedAuthority> authorities = converter.convert(jwt);
        Set<String> authorityValues = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        // Verificación: Verificar el resultado esperado
        assertTrue(authorityValues.contains("ROLE_ADMIN"),
                "Debe mapear admin y ROLE_ADMIN a ROLE_ADMIN");
        assertTrue(authorityValues.contains("ROLE_CLIENT"),
                "Debe mapear roles de resource_access a ROLE_<UPPERCASE>");
        assertEquals(2, authorityValues.size(),
                "No debe generar autoridades duplicadas");
    }

    @Test
    @DisplayName("JwtRoleConverter usa clientId de propiedad")
    void JwtRoleConverter_usaClientIdDePropiedad() {
        // Setup: Preparar el escenario
        JwtRoleConverter converter = new JwtRoleConverter("web");
        Jwt jwt = jwtWithClaims(
                List.of(),
                null,
                Map.of(
                        "web", Map.of("roles", List.of("admin")),
                        "other", Map.of("roles", List.of("client"))));

        // Ejercitación: Ejecutar la acción a probar
        Set<String> authorities = converter.convert(jwt).stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        // Verificación: Verificar el resultado esperado
        assertEquals(Set.of("ROLE_ADMIN"), authorities,
                "Debe usar primero security.keycloak.client-id para resource_access");
    }

    @Test
    @DisplayName("JwtRoleConverter usa fallback azp")
    void JwtRoleConverter_usaFallbackAzp() {
        // Setup: Preparar el escenario
        JwtRoleConverter converter = new JwtRoleConverter();
        Jwt jwt = jwtWithClaims(
                List.of(),
                "mobile-app",
                Map.of(
                        "mobile-app", Map.of("roles", List.of("admin")),
                        "other", Map.of("roles", List.of("client"))));

        // Ejercitación: Ejecutar la acción a probar
        Set<String> authorities = converter.convert(jwt).stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        // Verificación: Verificar el resultado esperado
        assertEquals(Set.of("ROLE_ADMIN"), authorities,
                "Debe usar azp cuando no hay client-id configurado");
    }

    @Test
    @DisplayName("JwtRoleConverter usa fallback merge resource access")
    void JwtRoleConverter_usaFallbackMergeResourceAccess() {
        // Setup: Preparar el escenario
        JwtRoleConverter converter = new JwtRoleConverter();
        Jwt jwt = jwtWithClaims(
                List.of(),
                null,
                Map.of(
                        "web", Map.of("roles", List.of("admin")),
                        "mobile", Map.of("roles", List.of("client", "ROLE_ADMIN"))));

        // Ejercitación: Ejecutar la acción a probar
        Set<String> authorities = converter.convert(jwt).stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        // Verificación: Verificar el resultado esperado
        assertEquals(Set.of("ROLE_ADMIN", "ROLE_CLIENT"), authorities,
                "Debe unir roles de todos los clientes si no hay client-id ni azp");
    }

    private Jwt jwtWithRoles(List<String> realmRoles, Map<String, Object> resourceAccess) {
        return jwtWithClaims(realmRoles, null, resourceAccess);
    }

    private Jwt jwtWithClaims(List<String> realmRoles, String azp, Map<String, Object> resourceAccess) {
        var builder = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("realm_access", Map.of("roles", realmRoles))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300));

        if (azp != null) {
            builder.claim("azp", azp);
        }
        if (resourceAccess != null) {
            builder.claim("resource_access", resourceAccess);
        }

        return builder.build();
    }
}
