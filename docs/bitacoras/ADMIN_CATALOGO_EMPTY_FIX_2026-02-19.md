# ADMIN Catalogo Empty — Fix report (2026-02-19)

**Symptom**

- Admin UI route `/admin/catalogo` loads HTML but the catalog list shows the empty state: "No hay películas para mostrar" despite movies existing in the DB.
- DevTools screenshot showed the page HTML (served from Vite dev server) but did not by itself show the API XHR result.

Evidence collected

- From the backend/network: an unauthenticated request to the gateway admin endpoint returned 401 Unauthorized:
  - Request: `GET http://localhost:9500/api/admin/peliculas`
  - Result observed (PowerShell): HTTP 401 (Unauthorized)
- Frontend code audit shows the admin client uses a correct base: `API_BASE` defaults to `http://localhost:9500/api` in `src/api/config.js`.
- Frontend admin code calls the adapter `listMovies(accessToken, { q, page, size, sort, asc })` in `src/pages/AdminCatalogo.jsx` on mount and on page changes.
- The admin adapter `src/api/catalogoAdmin.js`:
  - builds URL `${API_BASE}/admin/peliculas?...` and attaches header `Authorization: Bearer ${accessToken}`.
  - normalizes array responses into `{ items, total }` when backend returns a plain array.
- Backend audit:
  - The admin controller `PeliculaAdminController` previously returned a plain array; public controller returned `PageResponse`.
  - Security is enforced in `SecurityConfig` with a JWT decoder and a `KeycloakRealmRoleConverter` that previously mapped roles as-is into authorities.

Root cause (primary)

- Two issues combined:
  1. Admin endpoint response contract mismatch (backend returned plain array for admin list while frontend expects a paginated object with `items` + pagination metadata). This made the UI rely on adapter normalization but without full metadata.
  2. Role/authority mapping fragility between Keycloak roles and Spring Security authority checks: the app expects/uses `ROLE_ADMIN` in checks and in `AuthContext`, while Keycloak realm roles are raw (e.g., `admin`). When the authority mapping doesn't provide `ROLE_ADMIN`, requests are rejected (401/403) and the UI receives no items.

Fix summary (minimal and local)

- Make admin endpoint return a consistent paginated contract (same as public) and keep backend pagination/sorting applied.
- Make Keycloak role conversion robust: also expose a `ROLE_`-prefixed authority (`ROLE_<UPPERCASE>`) for each realm role so both `admin` and `ROLE_ADMIN` checks succeed.
- Add integration tests covering pagination mapping and security mapping.
- No Docker/Compose changes were performed.

Files changed (high level)

- Backend (API module)
  - `src/main/java/unrn/api/PeliculaAdminController.java`
    - Replaced plain-array `listar()` with a paginated `listar(...)` that accepts the same query parameters as the public endpoint and returns `ResponseEntity<PageResponse<DetallePeliculaDTO>>`.
  - `src/main/java/unrn/api/PageResponse.java` (already present and used by public controller)
  - `src/main/java/unrn/config/SecurityConfig.java`
    - `KeycloakRealmRoleConverter` updated to add `ROLE_` prefixed authorities for each realm role (keeps the raw role too). This ensures `hasAuthority("ROLE_ADMIN")` and `hasRole("ADMIN")` checks work with Keycloak tokens.
  - `pom.xml`
    - Added `spring-security-test` as a test dependency to allow JWT post-processor in tests.

- Tests added
  - `src/test/java/unrn/service/AdminPeliculasControllerIntegrationTest.java`
    - Integration tests validating: paged response metadata, items length for page/size, sorting behavior.
  - `src/test/java/unrn/service/AdminPeliculasSecurityIntegrationTest.java`
    - Security integration test verifying: unauthenticated -> 401, authenticated without admin -> 403, authenticated with `ROLE_ADMIN` -> 200, and authenticated with raw `admin` role -> 200 (conversion works).

Key code snippets (what changed)

- `PeliculaAdminController.listar(...)` now calls `peliculaService.buscarPaginado(...)`, maps `Page<Pelicula>` to DTO list and returns `new PageResponse<>(items, totalElements, totalPages, pageNumber, pageSize)`.

- `SecurityConfig.KeycloakRealmRoleConverter` updated to produce both the raw role and a `ROLE_`-prefixed uppercased variant:
  - Example behavior: for realm role `admin` the converter will emit authorities: `admin` and `ROLE_ADMIN`.

Tests added (what they assert)

- `listarAdmin_conDatos_devuelvePageResponseConItemsYTotalCorrectos`
  - Creates director/actor and one movie in test DB, calls admin listing, asserts response JSON contains `$.items.length()==1`, and presence of `$.total`, `$.page`, `$.size`, `$.totalPages`.
- `listarAdmin_conPageSize_devuelveCantidadEsperada`
  - Creates 3 movies, requests `size=2&page=0`, asserts `$.items.length()==2` and `$.total` present.
- `listarAdmin_conSortDesc_respetaOrden`
  - Creates two movies `AAA` and `ZZZ`, requests `sort=titulo&asc=false`, asserts `$.items[0].titulo == "ZZZ"`.
- `listarAdmin_sinRolAdmin_denegado`
  - Uses `spring-security-test` JWT post-processor to assert:
    - unauthenticated -> `401`
    - authenticated w/o roles -> `403`
    - authenticated with `ROLE_ADMIN` -> `200`
    - authenticated with raw `admin` role -> `200` (converter added mapping)

How to verify manually (browser + curl)

1. Browser: DevTools → Network
   - Filter for `peliculas` and `9500`.
   - Visit `http://localhost:5173/admin/catalogo`.
   - Confirm there is an XHR to `http://localhost:9500/api/admin/peliculas?page=0&size=12...`.
   - Check the request headers: `Authorization: Bearer <JWT>` should be present.
   - Check response status:
     - If 200 -> response body should be a paginated object: `{ items: [...], total: <number>, totalPages: <number>, page: <number>, size: <number> }`.
     - If 401/403 -> token/roles problem (see next step).

2. Quick curl checks (from developer machine):

- Unauthenticated (expect 401):

  ```powershell
  Invoke-WebRequest -UseBasicParsing -Uri 'http://localhost:9500/api/admin/peliculas' -Method GET
  ```

- With invalid token (expect 401):

  ```powershell
  Invoke-WebRequest -UseBasicParsing -Uri 'http://localhost:9500/api/admin/peliculas' -Method GET -Headers @{ Authorization = 'Bearer invalid' }
  ```

- With a real admin token (expect 200 and paginated body):

  ```bash
  curl -H "Authorization: Bearer <ADMIN_JWT>" "http://localhost:9500/api/admin/peliculas?page=0&size=10&sort=titulo&asc=true"
  ```

3. Run backend tests (module):

```bash
cd el-almacen-de-peliculas-online/el-almacen-de-peliculas-online
mvn -Dtest=AdminPeliculasSecurityIntegrationTest test
mvn -Dtest=AdminPeliculasControllerIntegrationTest test
```

Notes & rationale

- The adapter in `catalogoAdmin.js` already provided backward-compatible normalization for array responses; however returning the full paginated contract from the admin endpoint removes ambiguity and prevents edge cases where metadata is missing.
- Converting Keycloak roles to both raw and `ROLE_`-prefixed authorities is minimally invasive and keeps existing `hasAuthority("ROLE_ADMIN")` checks working while supporting default Keycloak role naming.
- No Docker or compose changes were made.

Files changed (exact paths)

- el-almacen-de-peliculas-online/el-almacen-de-peliculas-online/src/main/java/unrn/api/PeliculaAdminController.java
- el-almacen-de-peliculas-online/el-almacen-de-peliculas-online/src/main/java/unrn/config/SecurityConfig.java
- el-almacen-de-peliculas-online/el-almacen-de-peliculas-online/pom.xml (test dependency added)
- el-almacen-de-peliculas-online/el-almacen-de-peliculas-online/src/test/java/unrn/service/AdminPeliculasControllerIntegrationTest.java
- el-almacen-de-peliculas-online/el-almacen-de-peliculas-online/src/test/java/unrn/service/AdminPeliculasSecurityIntegrationTest.java
- docs/bitacoras/ADMIN_CATALOGO_EMPTY_FIX_2026-02-19.md (this file)

Final status

- Server-side: admin endpoint now returns a `PageResponse` (matching public contract) and the role converter maps Keycloak roles into `ROLE_`-prefixed authorities, resolving common role-name mismatches.
- Tests: new integration tests added to validate pagination and security. Run them locally with Maven as shown above.

If you'd like, next steps I can take now

- Run the full test suite (takes longer) and provide the results.
- Add a small frontend unit test (Vitest) for `normalizeListPayload` in `src/api/catalogoAdmin.js` to ensure array/object normalization behaviors are covered.
- Instrument a short DEV-only console log in `catalogoAdmin.listMovies` to print the final URL and headers when `import.meta.env.DEV` is true (helpful for fast local verification), guarded so it doesn't ship to production.

No Docker/Compose files were modified in this fix.

---

Report generated by the debugging session on 2026-02-19.
