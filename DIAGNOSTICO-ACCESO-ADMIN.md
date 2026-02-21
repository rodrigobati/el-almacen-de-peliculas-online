**Summary**

- Short: A non-admin user (`patito`) can render the admin catalog page because the frontend allows any authenticated user to access `/admin/*` and the admin catalog UI fetches its movie list from a public endpoint (`/api/peliculas`). Admin actions (create/update/delete) correctly call protected endpoints (`/api/admin/**`) and fail with 403 for `patito`.

**Reproduction Steps**

1. Log in as user `patito` in the frontend (Keycloak SSO).
2. Open URL: `http://localhost:5173/admin/catalogo`.
3. Observe Admin UI renders and lists movies.
4. Try an admin action (e.g., Retirar). The UI shows a toast: “No se pudo retirar – HTTP 403”.

**Evidence Collected**

- Admin page list fetch
  - Source in frontend: `src/pages/AdminCatalogo.jsx` imports `listMovies` from `src/api/catalogoAdmin.js` and calls it to populate the table. See: [src/pages/AdminCatalogo.jsx](el-almacen-de-peliculas-online-front-end/src/pages/AdminCatalogo.jsx)
  - Implementation: `listMovies` builds a GET to `${API_BASE}/peliculas?...` and calls `fetch(url)` with NO `Authorization` header. See: [src/api/catalogoAdmin.js](el-almacen-de-peliculas-online-front-end/src/api/catalogoAdmin.js)
  - Effective request URL (runtime): `http://localhost:9500/api/peliculas?...` (derived from `src/api/config.js`). See: [src/api/config.js](el-almacen-de-peliculas-online-front-end/src/api/config.js)
  - Method: GET. No auth header added by code -> public fetch.

- Admin action failing request (retire/edit)
  - Source in frontend: `retireMovie(...)` and `updateMovie(...)` call `${API_BASE}/admin/peliculas/...` and include `Authorization: Bearer <accessToken>` in headers. See: [src/api/catalogoAdmin.js](el-almacen-de-peliculas-online-front-end/src/api/catalogoAdmin.js)
  - Observed runtime behavior (from UI): retire/edit requests return HTTP 403 for `patito` (UI toast: “No se pudo retirar – HTTP 403”). This matches the code path that sends Authorization and receives 403 from backend.
  - Method: DELETE/PUT/POST to `/api/admin/peliculas/...` with Authorization header.

**Token Roles (Redacted)**

- How the frontend obtains roles: `AuthContext` uses Keycloak service `getUserInfo()` (which returns `keycloak.tokenParsed`) and extracts roles from `realm_access.roles` and `resource_access.*.roles`. See: [src/contexts/AuthContext.jsx](el-almacen-de-peliculas-online-front-end/src/contexts/AuthContext.jsx)
- Where Keycloak is configured: [src/services/keycloak.js](el-almacen-de-peliculas-online-front-end/src/services/keycloak.js)
- What to capture (do this in the browser to confirm `patito` claims):
  1. Open DevTools -> Network. Trigger any admin action that includes Authorization header (e.g., try Retirar). Select the request and confirm `Request Headers` contains `Authorization: Bearer <token>` (do NOT paste full token into reports).
  2. In Console, run (redact token before sharing):

```javascript
// copy token from request header into `token` variable in console
const token = '<paste-token-here-redact-before-sharing>'
const claims = JSON.parse(atob(token.split('.')[1]));
console.log({realm_access: claims.realm_access, resource_access: claims.resource_access});
```

  - Expected: you'll see `realm_access.roles` and `resource_access` entries. For `patito` the 403 observed implies the token does NOT contain the admin role expected by the backend (e.g., `ROLE_ADMIN` or similar). Confirm and paste only role names (no token) into the report.

**Findings**

- Frontend routing / guards
  - The route `/admin/catalogo` is wrapped with `ProtectedRoute` which only checks `isAuthenticated` (not roles). See: [src/components/ProtectedRoute.jsx](el-almacen-de-peliculas-online-front-end/src/components/ProtectedRoute.jsx)
  - Therefore any authenticated user (not just admins) can navigate to and render the admin UI.

- Which API endpoints are used
  - Movie list: `GET /api/peliculas?...` (public endpoint, no Authorization header in code). See: [src/api/catalogoAdmin.js](el-almacen-de-peliculas-online-front-end/src/api/catalogoAdmin.js)
  - Admin actions (create/update/delete, directors/actors admin endpoints): `POST/PUT/DELETE /api/admin/...` with `Authorization: Bearer <token>` header. See: [src/api/catalogoAdmin.js](el-almacen-de-peliculas-online-front-end/src/api/catalogoAdmin.js)

- Backend / gateway enforcement observations
  - Backend correctly returns 403 for admin actions when `patito` attempts them (evidence: UI toast and code sends Authorization header; backend denies with 403).
  - The movie list is served by the public endpoint `/api/peliculas` so the backend is intentionally returning data to unauthenticated/unauthorized clients for list requests.

**Root Cause (choose one)**

- Primary classification: C2 — Admin page uses public endpoints for listing, leaking data via public API.

  Rationale: two related problems exist: (1) The frontend allows any authenticated user to reach `/admin/*` (no role-based guard), and (2) the admin catalog UI obtains its movie list from the public `GET /api/peliculas` endpoint. The immediate reason `patito` can *see* the admin catalog data is that the list fetch calls a public endpoint (C2). If the admin list used `/api/admin/...` instead, the backend would have returned 403 and the page would show an error instead of listing items. Therefore the best single classification explaining the data leak is C2.

**Recommendations (prioritized)**

- Mandatory (frontend): Add a role-based route guard for all `/admin/*` routes.
  - What to change (high-level): update `ProtectedRoute` or add a new `AdminRoute` that checks `roles` from `useAuth()` (extracted from `keycloak.tokenParsed`) for the expected admin role name (e.g., `ROLE_ADMIN`, `ADMIN`, or `catalog-admin` depending on Keycloak mapping). Files/areas: `src/components/ProtectedRoute.jsx`, `src/pages/*` where admin pages are routed (App.jsx).

- Mandatory (backend): Enforce server-side authorization for ALL `/api/admin/**` endpoints including GET/list endpoints.
  - What to change (high-level): ensure Spring Security (or API Gateway) configuration requires admin role for `/api/admin/**` (both GET and non-GET). Files/areas to review: backend service security config or API gateway routing/security (e.g., Spring Security config classes in `apigateway` or `catalog` service). Do NOT change Docker files.

- Optional (frontend UX): Hide admin navigation links for non-admin users and do not render admin UI components if roles are missing (defensive UX).
  - What to change (high-level): in `App.jsx` check `roles` returned by `useAuth()` before rendering the admin nav link; do not expose admin screens in the DOM for non-admins. Files/areas: `src/App.jsx`, `src/components/ProtectedRoute.jsx`.

**Next Verification Checklist (how to confirm fixes later)**

1. Frontend route guard verification
   - As `patito`, visit `http://localhost:5173/admin/catalogo`.
   - Expected: redirect away (e.g., to `/` or “Acceso denegado” UI). No admin UI components should render.
   - Code to check: `ProtectedRoute` or `AdminRoute` enforces role check using `useAuth().roles`.

2. Backend enforcement verification
   - As `patito`, call `GET /api/admin/peliculas` (curl or browser) and confirm HTTP 403.
   - Admin users should receive 200 for `GET /api/admin/peliculas`.
   - Check Spring Security config (or gateway rules) ensure `/api/admin/**` requires admin role.

3. Data exposure regression check
   - Confirm admin UI uses `/api/admin/peliculas` (protected) for listing after fix, or that the public `/api/peliculas` returns only fields intended for public consumption.

4. Token/role verification
   - Re-run token inspection steps (Network → request with Authorization header → decode token payload) and confirm `patito` lacks admin roles while an admin account includes them.

**Files referenced**

- Frontend routing and auth: [src/App.jsx](el-almacen-de-peliculas-online-front-end/src/App.jsx), [src/components/ProtectedRoute.jsx](el-almacen-de-peliculas-online-front-end/src/components/ProtectedRoute.jsx), [src/contexts/AuthContext.jsx](el-almacen-de-peliculas-online-front-end/src/contexts/AuthContext.jsx)
- Frontend API clients: [src/api/catalogoAdmin.js](el-almacen-de-peliculas-online-front-end/src/api/catalogoAdmin.js), [src/api/movies.js](el-almacen-de-peliculas-online-front-end/src/api/movies.js), [src/api/config.js](el-almacen-de-peliculas-online-front-end/src/api/config.js)
- Keycloak setup: [src/services/keycloak.js](el-almacen-de-peliculas-online-front-end/src/services/keycloak.js)

**No Docker changes were made.**

If you want, I can now:
- Provide a minimal, safe code patch that (A) adds role-checking to `ProtectedRoute` and (B) updates admin list usage to call `/api/admin/peliculas` (or at least gate the admin UI behind role checks). I will not apply those changes until you approve.