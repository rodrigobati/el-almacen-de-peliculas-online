# SECURITY ROLE UNIFICATION — Step 1.1

## Objetivo
Endurecer la unificación de roles para que la autorización se base en authorities canónicas `ROLE_<UPPERCASE>`, con extracción determinística de roles de Keycloak y validación por tests basados en claims JWT.

## Cambios implementados

### 1) Normalización y estrategia determinística de `resource_access`
Se aplicó en los converters de los servicios:
- `security.keycloak.client-id` (si está configurado)
- fallback `azp`
- fallback merge de `resource_access.*.roles`

Además:
- canonicalización a `ROLE_<UPPERCASE>`
- deduplicación de authorities
- soporte uniforme para `realm_access.roles` + `resource_access`

Archivos:
- `el-almacen-de-peliculas-online/src/main/java/unrn/config/JwtRoleConverter.java`
- `el-almacen-de-peliculas-online-rating/src/main/java/unrn/rating/config/KeycloakGrantedAuthoritiesConverter.java`
- `el-almacen-de-peliculas-online-ventas/src/main/java/unrn/security/JwtRoleConverter.java`
- `el-almacen-de-peliculas-online-keycloak/src/main/java/ar/unrn/video/config/KeycloakGrantedAuthoritiesConverter.java`

### 2) Configuración de seguridad para usar converter canónico
Se verificó/ajustó wiring del converter en cada servicio y uso consistente de `hasRole("ADMIN")` donde aplica.

Archivos:
- `el-almacen-de-peliculas-online/src/main/java/unrn/config/SecurityConfig.java`
- `el-almacen-de-peliculas-online-rating/src/main/java/unrn/rating/config/SecurityConfiguration.java`
- `el-almacen-de-peliculas-online-ventas/src/main/java/unrn/security/SecurityConfig.java`
- `el-almacen-de-peliculas-online-keycloak/src/main/java/ar/unrn/video/config/SecurityConfiguration.java`
- `el-almacen-de-peliculas-online-keycloak/src/main/java/ar/unrn/video/rest/MovieResource.java`
- `el-almacen-de-peliculas-online-keycloak/src/main/java/ar/unrn/video/rest/TestResource.java`

### 3) Tests claims-based por servicio
Se reforzaron tests para validar conversión desde claims JWT (sin inyectar authorities preconstruidas):

Main:
- `el-almacen-de-peliculas-online/src/test/java/unrn/config/JwtRoleConverterTest.java`
- `el-almacen-de-peliculas-online/src/test/java/unrn/service/AdminPeliculasSecurityIntegrationTest.java`

Rating:
- `el-almacen-de-peliculas-online-rating/src/test/java/unrn/rating/config/KeycloakGrantedAuthoritiesConverterTest.java`
- `el-almacen-de-peliculas-online-rating/src/test/java/unrn/rating/security/RatingSecurityClaimsIntegrationTest.java`

Ventas:
- `el-almacen-de-peliculas-online-ventas/src/test/java/unrn/security/JwtRoleConverterTest.java`
- `el-almacen-de-peliculas-online-ventas/src/test/java/unrn/security/ApiSecurityProdLikeIntegrationTest.java`

Keycloak:
- `el-almacen-de-peliculas-online-keycloak/src/test/java/ar/unrn/video/config/KeycloakAdminSecurityClaimsIntegrationTest.java`

## Verificación ejecutada
Se ejecutaron los tests focalizados de Step 1.1 en `main`, `rating`, `ventas` y `keycloak` y quedaron en verde.

## Resultado funcional
- Se elimina dependencia de checks ambiguos/raw de roles.
- La decisión de autorización usa authorities canónicas y estrategia determinística para roles de cliente Keycloak.
- La validación automatizada cubre escenarios realm/resource claims y deduplicación.

## Restricciones de infraestructura
**No Docker changes were made.**
