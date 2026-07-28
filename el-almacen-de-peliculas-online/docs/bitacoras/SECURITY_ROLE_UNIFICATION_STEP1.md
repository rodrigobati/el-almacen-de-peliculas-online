# SECURITY ROLE UNIFICATION — STEP 1

## 1. Previous State

- La conversión JWT → autoridades era inconsistente entre servicios.
- `el-almacen-de-peliculas-online` emitía doble autoridad para algunos roles (`admin` y `ROLE_ADMIN`).
- `el-almacen-de-peliculas-online-rating` y `el-almacen-de-peliculas-online-keycloak` usaban `GrantedAuthorityDefaults("")`, removiendo el prefijo estándar `ROLE_`.
- En algunos recursos había chequeos `hasRole('ROLE_ADMIN')`, que no sigue el patrón idiomático de Spring (`hasRole('ADMIN')`).
- Existía un test que validaba aceptación explícita de rol crudo `admin`.

## 2. Canonical Decision

- Convención interna única adoptada: **todas las autoridades en Spring tienen forma `ROLE_<UPPERCASE>`**.
- Los chequeos de autorización pasan a convención canónica con `hasRole("ADMIN")`.
- Se elimina la emisión dual y el soporte estructural para roles crudos como autoridad final.
- No se usa `GrantedAuthorityDefaults("")`.

## 3. Implementation Details

### Converter logic

- Se implementó conversión canónica con estas reglas en los servicios actualizados:
  - lee roles desde `realm_access.roles`;
  - lee roles desde `resource_access.<clientId>.roles`;
  - unifica ambas fuentes;
  - para cada rol: quita prefijo `ROLE_` si existe, convierte a mayúsculas y emite una sola autoridad `ROLE_<UPPERCASE>`;
  - elimina duplicados.

### SecurityConfig changes

- `el-almacen-de-peliculas-online`:
  - `hasAuthority("ROLE_ADMIN")` → `hasRole("ADMIN")`.
  - `JwtAuthenticationConverter` ahora usa `JwtRoleConverter` canónico.
- `el-almacen-de-peliculas-online-rating`:
  - removido `GrantedAuthorityDefaults("")`.
  - `KeycloakGrantedAuthoritiesConverter` migrado a conversión canónica (realm + resource).
- `el-almacen-de-peliculas-online-ventas`:
  - agregado `JwtRoleConverter` canónico.
  - inyectado `jwtAuthenticationConverter(...)` en `oauth2ResourceServer().jwt(...)` del perfil productivo.
- `el-almacen-de-peliculas-online-keycloak`:
  - removido `GrantedAuthorityDefaults("")`.
  - `KeycloakGrantedAuthoritiesConverter` migrado a conversión canónica (realm + resource).
  - `@PreAuthorize("hasRole('ROLE_ADMIN')")` → `@PreAuthorize("hasRole('ADMIN')")`.

### Services modified

- main service (`el-almacen-de-peliculas-online`)
- rating service (`el-almacen-de-peliculas-online-rating`)
- ventas service (`el-almacen-de-peliculas-online-ventas`)
- keycloak auxiliary service (`el-almacen-de-peliculas-online-keycloak`)

## 4. Test Adjustments

### Tests updated

- `AdminPeliculasSecurityIntegrationTest#listarAdmin_sinRolAdmin_denegado`
  - mantiene verificación 401/403/200.
  - ya no valida aceptación de rol crudo; ahora valida que `admin` crudo sea **403**.
  - se corrigió inicialización de `MockMvc` aplicando `springSecurity()` para ejecutar realmente el filtro de seguridad.

### Tests added

- `JwtRoleConverterTest#JwtRoleConverter_emitsCanonicalRole`
  - verifica mapeo de `admin` → `ROLE_ADMIN`.
  - verifica mapeo de `ROLE_ADMIN` → `ROLE_ADMIN`.
  - verifica lectura combinada de `realm_access` y `resource_access`.
  - verifica ausencia de duplicados.

## 5. Resulting Behavior

- Internamente, Spring Security recibe únicamente autoridades canónicas con prefijo `ROLE_` y en mayúsculas.
- Ejemplo:
  - rol JWT `admin` → autoridad Spring `ROLE_ADMIN`.
  - rol JWT `ROLE_ADMIN` → autoridad Spring `ROLE_ADMIN`.
  - rol JWT `client` (en `resource_access`) → autoridad Spring `ROLE_CLIENT`.

## 6. Risks and Compatibility Notes

- **Compatibilidad**: las autoridades crudas (por ejemplo `admin` sin prefijo) ya no se consideran válidas como resultado final de conversión canónica.
- Si existiera código externo que dependa explícitamente de autoridades sin prefijo, debe migrarse a la convención canónica.
- No se realizaron cambios de Docker/Compose.
