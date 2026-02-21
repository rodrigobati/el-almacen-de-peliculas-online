# Admin Catálogo — Artifact Request for Architectural Audit

## 1) Scope

- Validate architecture before request-level debugging.
- Confirm public/admin catalog API contract consistency (params + response shape).
- Confirm JWT role mapping and Spring Security conventions are coherent across services.
- Confirm frontend uses Gateway as API entrypoint and env-driven configuration.
- Confirm frontend data layer is aligned with backend paging contract.
- Confirm backend/frontend tests cover pagination, sorting, and security regression risks.
- Constraints for this request: do not modify Docker/Compose and do not require HTTP traces.

## 2) Required artifacts

### Backend (main service)

Provide these exact files:

- `el-almacen-de-peliculas-online/src/main/java/**/SecurityConfig.java`
- JWT/authority converter used by resource server (standalone class or inner class).
- `el-almacen-de-peliculas-online/src/main/java/**/PeliculaAdminController.java`
- `el-almacen-de-peliculas-online/src/main/java/**/PeliculaController.java`
- `el-almacen-de-peliculas-online/src/main/java/**/PageResponse.java` (or equivalent paging DTO)
- `el-almacen-de-peliculas-online/pom.xml` (security dependencies section)

Provide tree snippets:

- `el-almacen-de-peliculas-online/src/main/java/**` (relevant API/security/service/repository packages)
- `el-almacen-de-peliculas-online/src/test/java/**` (tests for admin/public catalog, pagination, sorting, security)

### Backend (other microservices using Keycloak/JWT)

For each service (e.g., rating, ventas if applicable), provide only security-related files:

- `*/src/main/java/**/SecurityConfig.java` or `SecurityConfiguration.java`
- JWT/authority converter classes (`*Jwt*Converter*.java`, `*Keycloak*Converter*.java`)
- Any `GrantedAuthorityDefaults` config
- Security-related tests under `*/src/test/java/**`

### Frontend

Provide these exact files:

- `el-almacen-de-peliculas-online-front-end/src/pages/AdminCatalogo.jsx`
- `el-almacen-de-peliculas-online-front-end/src/api/catalogoAdmin.js`
- `el-almacen-de-peliculas-online-front-end/src/api/config.js`
- Shared auth/token helpers used for Authorization header, typically:
  - `el-almacen-de-peliculas-online-front-end/src/contexts/AuthContext.jsx`
  - `el-almacen-de-peliculas-online-front-end/src/services/keycloak.js`
- Environment variable sources:
  - `.env`, `.env.local`, `.env.example`, and/or Vite config files where `VITE_API_BASE_URL` and Keycloak vars are defined

### Decoded JWT payload (admin user)

Provide one decoded admin JWT payload (claims JSON only; no header/signature) including at least:

- `iss`, `aud`, `sub`, `preferred_username`
- `realm_access.roles`
- `resource_access` (if present)

## 3) Optional artifacts

- Example JSON response body for public catalog paging endpoint.
- Example JSON response body for admin catalog paging endpoint.
- OpenAPI/spec/docs files if present (`docs/**`, `src/main/resources/**`).
- `realm-export.json` excerpt showing realm/client role definitions.

## 4) Collection commands (Linux/macOS, Windows PowerShell)

### Linux/macOS

```bash
# 1) Trees
find el-almacen-de-peliculas-online/src/main/java -type d | sort
find el-almacen-de-peliculas-online/src/test/java -type f -name "*.java" | sort

# 2) Locate required backend files
find el-almacen-de-peliculas-online/src/main/java -type f \( -name "SecurityConfig.java" -o -name "SecurityConfiguration.java" -o -name "*Jwt*Converter*.java" -o -name "*Keycloak*Converter*.java" -o -name "PeliculaAdminController.java" -o -name "PeliculaController.java" -o -name "PageResponse.java" \) | sort

# 3) Locate frontend files
find el-almacen-de-peliculas-online-front-end/src -type f \( -name "AdminCatalogo.jsx" -o -name "catalogoAdmin.js" -o -name "config.js" -o -name "AuthContext.jsx" -o -name "keycloak.js" \) | sort

# 4) Print contents (repeat per file path you found)
sed -n '1,260p' <FILE_PATH>

# 5) Security deps quick extract from pom.xml
grep -nEi "spring-security|oauth2|resource-server|security-test" el-almacen-de-peliculas-online/pom.xml

# 6) Decode JWT payload from env var JWT (claims only)
echo "$JWT" | cut -d '.' -f2 | tr '_-' '/+' | base64 --decode
```

### Windows PowerShell

```powershell
# 1) Trees
Get-ChildItem -Path .\el-almacen-de-peliculas-online\src\main\java -Directory -Recurse | Select-Object FullName
Get-ChildItem -Path .\el-almacen-de-peliculas-online\src\test\java -File -Recurse -Filter *.java | Select-Object FullName

# 2) Locate required backend files
Get-ChildItem -Path .\el-almacen-de-peliculas-online\src\main\java -File -Recurse |
  Where-Object {
    $_.Name -in @('SecurityConfig.java','SecurityConfiguration.java','PeliculaAdminController.java','PeliculaController.java','PageResponse.java') -or
    $_.Name -like '*Jwt*Converter*.java' -or
    $_.Name -like '*Keycloak*Converter*.java'
  } | Select-Object FullName

# 3) Locate frontend files
Get-ChildItem -Path .\el-almacen-de-peliculas-online-front-end\src -File -Recurse |
  Where-Object { $_.Name -in @('AdminCatalogo.jsx','catalogoAdmin.js','config.js','AuthContext.jsx','keycloak.js') } |
  Select-Object FullName

# 4) Print contents (repeat per file)
Get-Content <FILE_PATH> -TotalCount 260

# 5) Security deps quick extract from pom.xml
Select-String -Path .\el-almacen-de-peliculas-online\pom.xml -Pattern 'spring-security|oauth2|resource-server|security-test' -CaseSensitive:$false

# 6) Decode JWT payload from variable $JWT (claims only)
$payload = $JWT.Split('.')[1].Replace('-', '+').Replace('_', '/')
$pad = 4 - ($payload.Length % 4); if ($pad -lt 4) { $payload += '=' * $pad }
[Text.Encoding]::UTF8.GetString([Convert]::FromBase64String($payload))
```

## 5) Redaction rules

Redact/remove:

- Secrets, passwords, private keys, client secrets, full tokens, signatures.
- Sensitive env values (`DB_PASSWORD`, secret keys, credentials).

Keep intact (required for audit):

- File paths, package/class names, endpoint paths, config keys.
- JWT claim structure and role names exactly as issued (`realm_access.roles`, `resource_access`, etc.).
- Issuer and JWK set URI values.
- Paging field names (`items`, `total`, `page`, `size`, `totalPages`) and query param names.

## 6) Delivery format

- Preferred: paste artifacts in one message grouped by sections:
  - `BACKEND_MAIN`
  - `BACKEND_OTHER_SERVICES`
  - `FRONTEND`
  - `JWT_ADMIN_PAYLOAD`
  - `OPTIONAL`
- For each file use:
  - `----- BEGIN FILE: <repo-relative-path> -----`
  - `<content>`
  - `----- END FILE: <repo-relative-path> -----`
- Alternative: provide one ZIP containing the same files and section index.

## 7) Success criteria checklist

- [ ] Backend main security config + JWT converter provided.
- [ ] `PeliculaAdminController`, `PeliculaController`, `PageResponse` provided.
- [ ] Backend `pom.xml` security dependencies provided.
- [ ] Backend main/service test tree snippets provided.
- [ ] Other JWT-enabled services security files provided (or explicit “not applicable”).
- [ ] Frontend files (`AdminCatalogo.jsx`, `catalogoAdmin.js`, `config.js`, auth/token helpers) provided.
- [ ] Env variable sources for API base and Keycloak provided.
- [ ] One decoded admin JWT payload (claims only) provided.
- [ ] Optional JSON response examples and docs included (if available).
