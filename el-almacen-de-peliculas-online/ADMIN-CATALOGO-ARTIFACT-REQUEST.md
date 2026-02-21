# Admin Catálogo — Artifact Request for Architectural Audit

## 1) What we’re trying to validate (audit scope)

- API contract consistency between public and admin catalog endpoints (params + response shape).
- Security authority mapping (JWT → Spring Security) and a single convention across services.
- Gateway is the single frontend entrypoint and frontend uses env-driven API base.
- Frontend data-layer and adapter agreement with the server paging contract.
- Presence and coverage of automated tests that protect pagination, sorting and security behavior.

---

## 2) Required artifacts (must provide)

Provide the exact files listed (project-relative paths). Prefer raw file contents pasted in fenced blocks or a single ZIP (see Delivery format).

### 2.1 Backend (main service)

- Exact files (required)
  - `el-almacen-de-peliculas-online/src/main/java/**/SecurityConfig.java`
  - Any custom JWT → authorities converter used by the app (examples of likely names; supply exact path if different):
    - `el-almacen-de-peliculas-online/src/main/java/**/KeycloakRealmRoleConverter.java` OR the inner class inside `SecurityConfig.java`
    - or `JwtAuthenticationConverter` wrapper class if present
  - `el-almacen-de-peliculas-online/src/main/java/**/PeliculaAdminController.java`
  - `el-almacen-de-peliculas-online/src/main/java/**/PeliculaController.java`
  - `el-almacen-de-peliculas-online/src/main/java/**/PageResponse.java` (or equivalent paging DTO)
- Backend tree snippets needed (must include package structure)
  - `el-almacen-de-peliculas-online/src/main/java/` (list package folders under this path)
  - `el-almacen-de-peliculas-online/src/test/java/` (list tests under this path, especially any mentioning `peliculas`, `admin`, `security`, `pagination`)
- `pom.xml` security-related dependencies section (the `<dependencies>` block is fine; if very large, include only the dependencies that mention `security`, `oauth2`, `spring-security`, `spring-boot-starter-oauth2-resource-server`, `spring-security-test`)

### 2.2 Backend (other services if present)

For every other microservice that uses Keycloak/JWT (examples in this repo: `el-almacen-de-peliculas-online-rating`, `el-almacen-de-peliculas-online-ventas`):

- Security files (only security-related files required):
  - `*/src/main/java/**/SecurityConfiguration.java` or `SecurityConfig.java`
  - Any JWT/authority converter (e.g., `KeycloakGrantedAuthoritiesConverter.java`, `JwtRoleConverter.java`)
  - Any `GrantedAuthorityDefaults` bean configuration used to remove `ROLE_` prefix
- For each such service, also list package path under `src/main/java` and any test that asserts security behavior.

### 2.3 Frontend

- Exact files (required)
  - `el-almacen-de-peliculas-online-front-end/src/pages/AdminCatalogo.jsx`
  - `el-almacen-de-peliculas-online-front-end/src/api/catalogoAdmin.js`
  - `el-almacen-de-peliculas-online-front-end/src/api/config.js`
  - `el-almacen-de-peliculas-online-front-end/src/contexts/AuthContext.jsx`
  - `el-almacen-de-peliculas-online-front-end/src/services/keycloak.js`
- Environment variable sources (required)
  - `.env`, `.env.local`, `.env.example`, or Vite config where `VITE_API_BASE_URL` / Keycloak vars are defined

### 2.4 Example decoded JWT payload (admin user)

- Paste only the decoded JWT payload (JSON object of claims), remove signature/header. Redaction rules below.
- Include one token that represents an admin user (what Keycloak issues in your environment) so we can inspect `realm_access`, `resource_access`, `roles`, `preferred_username`, `aud`, and `iss` claims.

---

## 3) Optional artifacts (nice to have)

- Example JSON responses (full JSON bodies) for:
  - GET public page: `/peliculas?page=0&size=...&sort=...&asc=...`
  - GET admin page: `/api/admin/peliculas?page=0&size=...&sort=...&asc=...`
    (only response bodies — not HTTP traces)
- OpenAPI / Swagger / API docs if present (file(s) under `src/main/resources` or `docs/`).
- `realm-export.json` (Keycloak realm export) or excerpt showing role definitions (if easy to share).
- Any small README or comment that documents paging conventions.

---

## 4) How to collect (copy/paste commands)

Notes:

- Provide results as file contents in fenced code blocks labeled with path, or attach a single ZIP.
- If sensitive values exist, follow the redaction rules in Section 4.4.

### 4.1 Linux / macOS commands (copy/paste-ready)

List the repo root as working directory (run from the workspace root):

- Tree package lists (if `tree` installed)

```bash
# package tree for backend main service
tree -a -I target el-almacen-de-peliculas-online/src/main/java | sed -n '1,200p'

# tests tree
tree -a -I target el-almacen-de-peliculas-online/src/test/java | sed -n '1,200p'
```

- Find relevant files (portable)

```bash
# list exact files we requested
find el-almacen-de-peliculas-online -type f \
  -path "*/src/main/java/*" -name "SecurityConfig.java" -o -name "SecurityConfiguration.java" -o -name "Keycloak*Converter*.java" -o -name "PeliculaAdminController.java" -o -name "PeliculaController.java" -o -name "PageResponse.java" -print

# list frontend files
find el-almacen-de-peliculas-online-front-end -type f -path "*src/*" -name "AdminCatalogo.jsx" -o -name "catalogoAdmin.js" -o -name "config.js" -o -name "AuthContext.jsx" -o -name "keycloak.js" -print
```

- Dump file contents (one-shot)

```bash
# Example: print SecurityConfig.java
echo "----- el-almacen-de-peliculas-online/src/main/java/.../SecurityConfig.java -----"
sed -n '1,240p' el-almacen-de-peliculas-online/src/main/java/**/SecurityConfig.java

# Or to print each required file sequentially (safe, copy/paste into issue):
for f in \
  el-almacen-de-peliculas-online/src/main/java/**/SecurityConfig.java \
  el-almacen-de-peliculas-online/src/main/java/**/PeliculaAdminController.java \
  el-almacen-de-peliculas-online/src/main/java/**/PeliculaController.java \
  el-almacen-de-peliculas-online/src/main/java/**/PageResponse.java \
  el-almacen-de-peliculas-online/pom.xml \
  el-almacen-de-peliculas-online-front-end/src/pages/AdminCatalogo.jsx \
  el-almacen-de-peliculas-online-front-end/src/api/catalogoAdmin.js \
  el-almacen-de-peliculas-online-front-end/src/api/config.js \
  el-almacen-de-peliculas-online-front-end/src/contexts/AuthContext.jsx \
  el-almacen-de-peliculas-online-front-end/src/services/keycloak.js; do
  echo
  echo "===== FILE: $f ====="
  sed -n '1,400p' $f || echo "(file not found: $f)"
done
```

- Extract JWT payload (if you have a token string in shell)

```bash
# token in $JWT variable
echo "$JWT" | cut -d '.' -f2 | base64 --decode | jq .
```

### 4.2 Windows PowerShell commands (copy/paste-ready)

Run from repository root:

- List package/test trees

```powershell
# list backend packages (limited depth view)
Get-ChildItem -Directory -Recurse -Path .\el-almacen-de-peliculas-online\src\main\java\ -Depth 3 | Select-Object FullName

# list tests
Get-ChildItem -Recurse -Path .\el-almacen-de-peliculas-online\src\test\java\ -Filter *.java | Select-Object FullName
```

- Find and print specific files

```powershell
$files = @(
  ".\el-almacen-de-peliculas-online\src\main\java\**\SecurityConfig.java",
  ".\el-almacen-de-peliculas-online\src\main\java\**\PeliculaAdminController.java",
  ".\el-almacen-de-peliculas-online\src\main\java\**\PeliculaController.java",
  ".\el-almacen-de-peliculas-online\src\main\java\**\PageResponse.java",
  ".\el-almacen-de-peliculas-online\pom.xml",
  ".\el-almacen-de-peliculas-online-front-end\src\pages\AdminCatalogo.jsx",
  ".\el-almacen-de-peliculas-online-front-end\src\api\catalogoAdmin.js",
  ".\el-almacen-de-peliculas-online-front-end\src\api\config.js",
  ".\el-almacen-de-peliculas-online-front-end\src\contexts\AuthContext.jsx",
  ".\el-almacen-de-peliculas-online-front-end\src\services\keycloak.js"
)
foreach ($f in $files) {
  Write-Output "===== FILE: $f ====="
  if (Test-Path $f) { Get-Content $f -TotalCount 400 } else { Write-Output "(file not found: $f)" }
  Write-Output "`n"
}
```

- Decode JWT payload (PowerShell)

```powershell
# $JWT contains token
$payload = $JWT.Split('.')[1]
$pad = 4 - ($payload.Length % 4); if ($pad -lt 4) { $payload += '=' * $pad }
[System.Text.Encoding]::UTF8.GetString([System.Convert]::FromBase64String($payload)) | ConvertFrom-Json | ConvertTo-Json -Depth 5
```

### 4.3 If you prefer ZIP

From repo root (Linux/macOS):

```bash
zip -r admin-catalogue-artifacts.zip \
  el-almacen-de-peliculas-online/src/main/java/**/SecurityConfig.java \
  el-almacen-de-peliculas-online/src/main/java/**/PeliculaAdminController.java \
  el-almacen-de-peliculas-online/src/main/java/**/PeliculaController.java \
  el-almacen-de-peliculas-online/src/main/java/**/PageResponse.java \
  el-almacen-de-peliculas-online/pom.xml \
  el-almacen-de-peliculas-online/src/test/java/**/*peliculas*.java \
  el-almacen-de-peliculas-online-front-end/src/pages/AdminCatalogo.jsx \
  el-almacen-de-peliculas-online-front-end/src/api/catalogoAdmin.js \
  el-almacen-de-peliculas-online-front-end/src/api/config.js \
  el-almacen-de-peliculas-online-front-end/src/contexts/AuthContext.jsx \
  el-almacen-de-peliculas-online-front-end/src/services/keycloak.js
```

---

## 4.4 Redaction rules (what to remove vs what to keep)

- REMOVE / REDACT (mandatory):
  - Any private keys, `.jks` keystore passwords, client secrets, service-account secrets, OAuth client secrets, and full JWK private keys.
  - Any live access tokens, refresh tokens, or signatures. If pasting a JWT string, DO NOT paste the full token; instead paste only the decoded payload (claims) JSON as requested in Section 2.4.
  - Database passwords, production credentials, and any environment files containing secrets. If you must include `.env`, redact lines like `DB_PASSWORD=****`, `KEYCLOAK_SECRET=****`.

- KEEP (mandatory structural info):
  - Claim names and values in decoded JWT payload (e.g., `realm_access.roles`, `resource_access`, `preferred_username`, `iss`, `aud`) — these are required to audit authority mapping.
  - `issuer` (`iss`) and `jwk-set-uri` strings (so we can verify validator configuration).
  - Role names exactly as they appear in tokens (`admin`, `ROLE_ADMIN`, etc.) — do not change capitalization.
  - The full code for converters and security configuration (no redaction inside these files).
  - Paging DTO names and field names (e.g., `items`, `total`, `page`, `size`, `totalPages`).

---

## 5) Delivery format

- Preferred: Paste each file as a fenced code block using this header format and order (single message):
  - ```
    ----- BEGIN FILE: <repo-relative-path> -----
    <file content>
    ----- END FILE: <repo-relative-path> -----
    ```
  - Example: `----- BEGIN FILE: el-almacen-de-peliculas-online/src/main/java/unrn/config/SecurityConfig.java -----`
- Alternative: a single ZIP named `admin-catalogue-artifacts.zip` attached to the issue/PR.
- If pasting many files, group them by section headings: Backend main service → Backend other services → Frontend → JWT payload → Optional artifacts.
- Naming convention for pasted sections: `SERVICE-FILEPATH` (e.g., `backend-main: src/main/java/unrn/config/SecurityConfig.java`).

---

## 6) Success criteria

A "complete artifact set" for the audit contains all of the following:

- Backend main service:
  - `SecurityConfig.java` (or equivalent) and any JWT→authority converter(s).
  - `PeliculaAdminController.java`, `PeliculaController.java`, `PageResponse.java`.
  - `pom.xml` (or at least security-related dependencies).
  - Listing of `src/main/java` package tree and `src/test/java` tests related to `peliculas` and security.

- Backend other services (if present and using Keycloak):
  - Security config and JWT converters for each service.

- Frontend:
  - `AdminCatalogo.jsx`, `catalogoAdmin.js`, `config.js`, `AuthContext.jsx`, `keycloak.js`.
  - `.env.example` or other file showing `VITE_API_BASE_URL` and Keycloak environment variable names.

- Example decoded admin JWT payload (claims JSON) with roles present (redacted token string, but keep claims).

- Optional: example JSON paged responses for public and admin endpoints (if available).

If any required file cannot be provided, indicate which file and why (sensitive, missing, or not applicable).

---

If you paste the artifacts here I’ll proceed with the full architectural audit and produce the structured report (API contract analysis, security model recommendations, gateway and frontend fixes, test gaps and an actionable minimal remediation plan).
