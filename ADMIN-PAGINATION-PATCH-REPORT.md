Patch Summary

- Changed admin `/api/admin/peliculas` endpoint to return the same paginated contract as the public endpoint: `PageResponse<DetallePeliculaDTO>` with fields: `items`, `total`, `page`, `size`, `totalPages`.
- Preserved and forwarded the same query parameters supported by the public API: `q`, `page`, `size`, `sort`, `asc` (and additional filters supported by service).
- Ensured server-side pagination and sorting by calling `PeliculaService.buscarPaginado(...)` and mapping `Page<Pelicula>` -> `PageResponse<DetallePeliculaDTO>`.
- Secured GET `/api/admin/peliculas` to require `ROLE_ADMIN` (SecurityConfig updated).
- Added integration tests validating pagination metadata, page size, sorting and security access.
- No Docker/Compose changes made.

Files changed + key snippets

- Updated controller: [el-almacen-de-peliculas-online/el-almacen-de-peliculas-online/src/main/java/unrn/api/PeliculaAdminController.java](el-almacen-de-peliculas-online/el-almacen-de-peliculas-online/src/main/java/unrn/api/PeliculaAdminController.java#L1-L200)

Key snippet (method `listar`):

public ResponseEntity<PageResponse<DetallePeliculaDTO>> listar(... params ...) {
LocalDate d = (desde == null || desde.isBlank()) ? null : LocalDate.parse(desde);
LocalDate h = (hasta == null || hasta.isBlank()) ? null : LocalDate.parse(hasta);

    Page<Pelicula> pageResult = peliculaService.buscarPaginado(...);

    var dtoItems = pageResult.getContent().stream().map(DetallePeliculaDTO::from).toList();
    var response = new PageResponse<>(
            dtoItems,
            pageResult.getTotalElements(),
            pageResult.getTotalPages(),
            pageResult.getNumber(),
            pageResult.getSize());

    return ResponseEntity.ok(response);

}

- Security: [el-almacen-de-peliculas-online/el-almacen-de-peliculas-online/src/main/java/unrn/config/SecurityConfig.java](el-almacen-de-peliculas-online/el-almacen-de-peliculas-online/src/main/java/unrn/config/SecurityConfig.java#L1-L120)

Changed to require `ROLE_ADMIN` for GET `/api/admin/peliculas`:

.requestMatchers(HttpMethod.GET, "/api/admin/peliculas", "/api/admin/peliculas/\*\*").hasAuthority("ROLE_ADMIN")

- Added test dependency in POM: [el-almacen-de-peliculas-online/el-almacen-de-peliculas-online/pom.xml](el-almacen-de-peliculas-online/el-almacen-de-peliculas-online/pom.xml#L1)

Added:

<dependency>
  <groupId>org.springframework.security</groupId>
  <artifactId>spring-security-test</artifactId>
  <scope>test</scope>
</dependency>

Tests added

1. [el-almacen-de-peliculas-online/el-almacen-de-peliculas-online/src/test/java/unrn/service/AdminPeliculasControllerIntegrationTest.java](el-almacen-de-peliculas-online/el-almacen-de-peliculas-online/src/test/java/unrn/service/AdminPeliculasControllerIntegrationTest.java#L1-L300)

- listarAdmin_conDatos_devuelvePageResponseConItemsYTotalCorrectos
  - Setup: creates a director and actor, then creates a movie via POST.
  - Exercises: GET `/api/admin/peliculas` (with `size=10`).
  - Verifies: JSON contains `$.items` (length 1), `$.total`, `$.page`, `$.size`, `$.totalPages`.

- listarAdmin_conPageSize_devuelveCantidadEsperada
  - Setup: creates director/actor and 3 movies.
  - Exercises: GET `/api/admin/peliculas?page=0&size=2`.
  - Verifies: `$.items.length() == 2` and `$.total` is present.

- listarAdmin_conSortDesc_respetaOrden
  - Setup: creates two movies with titles `AAA` and `ZZZ`.
  - Exercises: GET `/api/admin/peliculas?sort=titulo&asc=false`.
  - Verifies: `$.items[0].titulo == "ZZZ"` (descending order respected).

Notes: These tests run with `@AutoConfigureMockMvc(addFilters = false)` to allow creating admin resources during setup without real JWT provisioning.

2. [el-almacen-de-peliculas-online/el-almacen-de-peliculas-online/src/test/java/unrn/service/AdminPeliculasSecurityIntegrationTest.java](el-almacen-de-peliculas-online/el-almacen-de-peliculas-online/src/test/java/unrn/service/AdminPeliculasSecurityIntegrationTest.java#L1-L200)

- listarAdmin_sinRolAdmin_denegado
  - Setup: boots application with security beans active.
  - Exercises/Verifies:
    - Unauthenticated GET `/api/admin/peliculas` -> HTTP 401 (Unauthorized).
    - Authenticated (JWT) without `ROLE_ADMIN` -> HTTP 403 (Forbidden).
    - Authenticated (JWT) with `ROLE_ADMIN` -> HTTP 200 (OK).
  - Uses `spring-security-test` JWT post-processor to simulate tokens in tests.

Verification steps (manual)

1. Start the application as usual (no Docker changes were made).

2. Example curl to fetch admin paged response (admin token required):

curl -s -H "Authorization: Bearer <ADMIN_JWT>" \
 'http://localhost:8080/api/admin/peliculas?page=0&size=10&sort=titulo&asc=true'

Expected JSON shape (200):

{
"items": [
{ "id": 1, "titulo": "Peli Test", "precio": 100.0, ... }
],
"total": 42,
"totalPages": 5,
"page": 0,
"size": 10
}

3. Example curl unauthenticated (should be 401):

curl -i 'http://localhost:8080/api/admin/peliculas'

Response: HTTP/1.1 401 Unauthorized

4. Example curl authenticated but without admin role (should be 403):

curl -i -H "Authorization: Bearer <USER_JWT_WITHOUT_ADMIN>" 'http://localhost:8080/api/admin/peliculas'

Response: HTTP/1.1 403 Forbidden

Notes / Constraints

- No Docker or Compose files were modified.
- All changes are limited to application code and tests.
- Existing public `/peliculas` endpoint and its contract were not modified.

No Docker changes were made.

\*\*\* End of report
