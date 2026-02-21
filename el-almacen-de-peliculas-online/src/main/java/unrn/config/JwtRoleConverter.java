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

public class JwtRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private final String configuredClientId;

    public JwtRoleConverter() {
        this(null);
    }

    public JwtRoleConverter(String configuredClientId) {
        this.configuredClientId = configuredClientId;
    }

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

    private String toCanonicalRole(String role) {
        String normalized = role.startsWith("ROLE_") ? role.substring("ROLE_".length()) : role;
        return "ROLE_" + normalized.toUpperCase();
    }

    @SuppressWarnings("unchecked")
    private List<String> resourceRolesByClientStrategy(Jwt jwt) {
        // Estrategia determinística:
        // 1) security.keycloak.client-id (si está configurado)
        // 2) claim azp del JWT
        // 3) fallback: merge de todos los resource_access.*.roles
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

    private String normalizeBlank(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
