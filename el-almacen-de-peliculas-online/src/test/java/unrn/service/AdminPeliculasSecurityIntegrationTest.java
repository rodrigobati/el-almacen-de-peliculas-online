package unrn.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import unrn.app.Application;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.Instant;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Application.class)
@TestPropertySource(properties = {
                "spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:9090/realms/videoclub",
                "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:9090/realms/videoclub/protocol/openid-connect/certs",
})
class AdminPeliculasSecurityIntegrationTest {

        @Autowired
        private WebApplicationContext webApplicationContext;

        @MockBean
        private JwtDecoder jwtDecoder;

        @Test
        @DisplayName("listarAdmin seguridad basada en claims JWT")
        void listarAdmin_seguridadBasadaEnClaimsJwt() throws Exception {
                MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                                .apply(springSecurity())
                                .build();

                // Sin autenticación -> 401
                mockMvc.perform(get("/api/admin/peliculas"))
                                .andExpect(status().isUnauthorized());

                // Token sin roles -> 403
                when(jwtDecoder.decode(anyString())).thenReturn(jwtSinRoles());
                mockMvc.perform(get("/api/admin/peliculas")
                                .header("Authorization", "Bearer token-sin-roles"))
                                .andExpect(status().isForbidden());

                // realm_access.roles incluye admin -> 200
                when(jwtDecoder.decode(anyString())).thenReturn(jwtConRealmRoleAdmin());
                mockMvc.perform(get("/api/admin/peliculas")
                                .header("Authorization", "Bearer token-realm-admin"))
                                .andExpect(status().isOk());

                // resource_access.<clientId>.roles con azp -> 200
                when(jwtDecoder.decode(anyString())).thenReturn(jwtConResourceRoleAdmin());
                mockMvc.perform(get("/api/admin/peliculas")
                                .header("Authorization", "Bearer token-resource-admin"))
                                .andExpect(status().isOk());
        }

        private Jwt jwtSinRoles() {
                return Jwt.withTokenValue("token")
                                .header("alg", "none")
                                .claim("sub", "user-1")
                                .issuedAt(Instant.now())
                                .expiresAt(Instant.now().plusSeconds(300))
                                .build();
        }

        private Jwt jwtConRealmRoleAdmin() {
                return Jwt.withTokenValue("token")
                                .header("alg", "none")
                                .claim("sub", "user-1")
                                .claim("realm_access", Map.of("roles", java.util.List.of("admin")))
                                .issuedAt(Instant.now())
                                .expiresAt(Instant.now().plusSeconds(300))
                                .build();
        }

        private Jwt jwtConResourceRoleAdmin() {
                return Jwt.withTokenValue("token")
                                .header("alg", "none")
                                .claim("sub", "user-1")
                                .claim("azp", "web")
                                .claim("resource_access", Map.of("web", Map.of("roles", java.util.List.of("admin"))))
                                .issuedAt(Instant.now())
                                .expiresAt(Instant.now().plusSeconds(300))
                                .build();
        }
}
