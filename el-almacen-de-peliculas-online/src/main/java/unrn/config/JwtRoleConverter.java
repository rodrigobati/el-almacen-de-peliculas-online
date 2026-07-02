package unrn.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Conversor de claims JWT de Keycloak a authorities de Spring Security.
 *
 * Lee roles del token, prioriza el client-id configurado y los normaliza al formato
 * ROLE_ que usa hasRole en la configuracion HTTP. Es el puente entre la identidad
 * emitida por Keycloak y las reglas de autorizacion de la API admin.
 */
public class JwtRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private final String configuredClientId;

    /**
     * Inicializa una instancia de JwtRoleConverter con los datos necesarios.
     */
    public JwtRoleConverter() {
        this(null);
    }

    /**
     * Inicializa una instancia de JwtRoleConverter con los datos necesarios.
     */
    public JwtRoleConverter(String configuredClientId) {
        this.configuredClientId = configuredClientId;
    }

    /**
     * Convierte los roles presentes en el JWT en authorities ROLE_* de Spring Security.
     * Combina roles de realm_access y resource_access para que las reglas hasRole puedan evaluarse.
     */
    @Override
    @SuppressWarnings("unchecked")
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Set<String> canonicalRoles = new LinkedHashSet<>();

        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        List<String> realmRoles = realmAccess == null ? List.of() : (List<String>) realmAccess.get("roles");
        canonicalRoles.addAll(toCanonicalRoles(realmRoles));

        canonicalRoles.addAll(toCanonicalRoles(resourceRolesByClientStrategy(jwt)));

        return canonicalRoles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    /**
     * Normaliza una lista de roles al formato ROLE_*.
     */
    private Set<String> toCanonicalRoles(List<String> roles) {
        if (roles == null) {
            return Set.of();
        }

        return roles.stream()
                .filter(java.util.Objects::nonNull)
                .map(String::trim)
                .filter(role -> !role.isEmpty())
                .map(this::toCanonicalRole)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Normaliza un rol individual al formato ROLE_*.
     */
    private String toCanonicalRole(String role) {
        String normalized = role.startsWith("ROLE_") ? role.substring("ROLE_".length()) : role;
        return "ROLE_" + normalized.toUpperCase();
    }

    /**
     * Obtiene roles de resource_access segun el cliente configurado o el claim azp.
     */
    @SuppressWarnings("unchecked")
    private List<String> resourceRolesByClientStrategy(Jwt jwt) {
        // Estrategia deterministica:
        // 1) security.keycloak.client-id, si esta configurado.
        // 2) claim azp del JWT.
        // 3) fallback: merge de todos los resource_access.*.roles.
        Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
        if (resourceAccess == null || resourceAccess.isEmpty()) {
            return List.of();
        }

        String clientIdFromProperty = normalizeBlank(configuredClientId);
        String clientIdFromAzp = normalizeBlank(jwt.getClaimAsString("azp"));
        String selectedClientId = clientIdFromProperty != null ? clientIdFromProperty : clientIdFromAzp;

        if (selectedClientId != null) {
            Object clientEntry = resourceAccess.get(selectedClientId);
            return extractRoles(clientEntry);
        }

        List<String> mergedRoles = new ArrayList<>();
        for (Object clientEntry : resourceAccess.values()) {
            mergedRoles.addAll(extractRoles(clientEntry));
        }
        return mergedRoles;
    }

    /**
     * Extrae la lista de roles desde la entrada resource_access de un cliente Keycloak.
     * Si el claim no tiene la estructura esperada, devuelve una lista vacia.
     */
    @SuppressWarnings("unchecked")
    private List<String> extractRoles(Object clientEntry) {
        if (!(clientEntry instanceof Map<?, ?> clientMap)) {
            return List.of();
        }
        Object rolesValue = clientMap.get("roles");
        if (!(rolesValue instanceof List<?> rolesList)) {
            return List.of();
        }
        List<String> roles = new ArrayList<>();
        for (Object role : rolesList) {
            if (role instanceof String roleName) {
                roles.add(roleName);
            }
        }
        return roles;
    }

    /**
     * Normaliza valores opcionales de configuracion o claims.
     * Convierte null y textos en blanco en null para simplificar la seleccion de client-id.
     */
    private String normalizeBlank(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
