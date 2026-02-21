# REBUILD & VALIDACIÓN — Admin Guard

Fecha: 2026-02-19

Resumen

---

Se ejecutó un rebuild limpio del frontend, se reconstruyeron las imágenes Docker usando `docker-compose-full.yml`, se levantó el stack y se realizaron comprobaciones de endpoints y logs para validar el hardening de acceso `/admin` aplicado en el frontend.

Resumen rápido de estado:

- Frontend built successfully (Vite production build).
- Docker images rebuilt (no-cache) and containers started.
- GET /api/peliculas (public) -> HTTP 200 (returns JSON list).
- GET /api/admin/peliculas without Authorization -> HTTP 401 (Unauthorized).
- Gateway logs show routing for /api/admin/\*\* and authorization checks.

Acciones ejecutadas (comandos y resultados clave)

---

A) Build frontend (local)

Path: el-almacen-de-peliculas-online-front-end

Commands run:

cd ...\el-almacen-de-peliculas-online-front-end

# removed existing artifacts (if any)

npm install
npm run build

Result (excerpt):

vite v5.4.20 building for production...
✓ 70 modules transformed.
dist/index.html 0.41 kB
dist/assets/index-8cWC_eC8.js 276.88 kB
✓ built in 699ms

Conclusion: frontend build succeeded without critical errors.

B) Docker rebuild / restart (project root)

Commands run (using provided docker-compose file):

cd ...\el-almacen-de-peliculas-online
docker compose -f docker-compose-full.yml build --no-cache
docker compose -f docker-compose-full.yml up -d
docker compose -f docker-compose-full.yml ps

Result (summary from build/run):

- Images rebuilt: peliculas-frontend, api-gateway, el-almacen-de-peliculas-online-backend, el-almacen-de-peliculas-online-ventas, el-almacen-de-peliculas-online-rating, etc.
- Containers reported healthy / running: peliculas-frontend, api-gateway, catalogo-backend, ventas-service, rating-service, keycloak, DB containers, RabbitMQ.

Notes: initial attempt to run docker compose without -f docker-compose-full.yml failed because default compose file name was not present; subsequent commands used the provided compose file explicitly.

C) Runtime endpoint checks (from host)

Commands run (host curl):

# public list (no auth)

curl -i http://localhost:9500/api/peliculas?size=1

# admin list (no auth)

curl -i http://localhost:9500/api/admin/peliculas?size=1

Observed responses (important parts):

- GET /api/peliculas?size=1

  HTTP/1.1 200 OK
  Content-Type: application/json

  Response body: JSON with items, total, page, size (example movie object returned).

- GET /api/admin/peliculas?size=1 (no Authorization header)

  HTTP/1.1 401 Unauthorized
  WWW-Authenticate: Bearer

Conclusion: admin endpoint is protected server-side (returns 401 when unauthenticated).

D) Gateway & backend logs (excerpt)

Collected recent logs from api-gateway and catalogo-backend.

Key observations from api-gateway logs:

- Requests to /api/peliculas and /api/admin/peliculas are matched to routes catalogo and catalogo-admin respectively.
- The gateway runs authorization checks (Spring Security AuthorizationWebFilter) for both routes.
- The gateway includes a TokenRelayGlobalFilter in its filter chain (used to forward tokens when present).

Excerpt (abridged, sanitized):

Route matched: catalogo
Mapping [Exchange: GET http://localhost:9500/api/peliculas?size=1] to Route{id='catalogo', uri=http://catalogo-backend:8080, ...}
DelegatingReactiveAuthorizationManager - Checking authorization on '/api/peliculas' ... Authorization successful

Route matched: catalogo-admin
Mapping [Exchange: GET http://localhost:9500/api/admin/peliculas?size=1] to Route{id='catalogo-admin', uri=http://catalogo-backend:8080, ...}
DelegatingReactiveAuthorizationManager - Checking authorization on '/api/admin/peliculas' ... Authorization successful

Notes: logs show gateway-level authorization checks; final enforcement for unauthenticated requests was a 401 from the backend when token absent.

E) Backend behavior

- catalogo-backend logs show SQL selects for movies and handling of requests.
- When unauthenticated requests hit /api/admin/peliculas the response observed was 401 (as above).

Limitations and manual checks required

---

- I could not programmatically obtain and use the Keycloak access token for the specific users (patito and admin) because credentials were not available to this automation session. Therefore I validated:
  - Unauthenticated access: admin endpoint returns 401 (good).
  - Public endpoint remains public and returns 200.

- To fully validate the non-admin vs admin behavior (403 vs 200 when token present): please perform the following manual steps (quick checklist):
  1.  In a browser, log in as non-admin patito and open http://localhost:5173/admin/catalogo:
      - Expected: UI shows Acceso denegado (403) or redirect; Network tab should NOT show GET /api/admin/peliculas with 200.
  2.  In DevTools, obtain the request to /api/admin/peliculas when logged as patito (if any) and verify status is 403.
  3.  Repeat as admin: login, open /admin/catalogo, verify GET /api/admin/peliculas includes Authorization: Bearer <token> and returns 200; test retire/edit/create actions succeed.

Evidence files and outputs

---

- Frontend build succeeded (Vite output) — captured during npm run build.
- Docker build/run produced image build logs and container health; services listed as running.
- Curl responses (headers + body) for public and admin endpoints captured (200 for public, 401 for admin without auth).
- Gateway logs show route matching and authorization checks.

Final verdict

---

Outcome: PARTIAL PASS

- PASS: Frontend compiled; Docker images rebuilt and the stack runs. Admin endpoints are protected server-side (401 when unauthenticated). Gateway routing and security filters are active.
- PARTIAL: I was unable to perform authenticated requests for patito and admin accounts (no credentials in this session). Therefore I could not demonstrate the 403-for-non-admin vs 200-for-admin behavior with real tokens—this needs a short manual verification by an operator who can log in to Keycloak as patito and an admin.

Recommended next manual validation steps (one-liner):

1.  Log in as patito in browser → visit /admin/catalogo → confirm Acceso denegado and no GET /api/admin/peliculas 200.
2.  Log in as admin → visit /admin/catalogo → confirm GET /api/admin/peliculas returns 200 and admin actions work.

If you want, I can guide you through the exact DevTools steps and curl commands to run with your tokens to capture the final 403 evidence and then update the report to PASS fully.

\*\*\* End
