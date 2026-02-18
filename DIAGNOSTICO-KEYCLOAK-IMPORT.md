# DIAGNOSTICO-KEYCLOAK-IMPORT

## 1. Executive summary

- Keycloak **sí importa ambos archivos JSON** en cada arranque del contenedor, usando comando explícito en runtime (`kc.sh import ...extras --override true` seguido de `kc.sh import ...main --override true`, luego `start-dev`).
- El mecanismo actual **no usa** `--import-realm` directo, sino una cadena en `entrypoint/cmd` que hace dos imports secuenciales con `--override true`.
- La importación deja estado final consistente con el **último archivo aplicado** (`realm-export.json`), no con `realm-export-extras.json`.
- Evidencia clave: `registrationAllowed` queda en `false` en runtime; en `realm-export-extras.json` está `true`, pero en `realm-export.json` está `false`.
- Esto explica el síntoma del botón de registro no visible: el realm final queda con registro deshabilitado.
- No se realizaron cambios; diagnóstico 100% read-only.

## 2. Current runtime wiring (compose + container inspect)

### Compose efectivo (`docker-compose-full.yml`)

Comando ejecutado:

```powershell
docker compose -f docker-compose-full.yml config
```

Fragmento relevante (recortado):

```yaml
keycloak:
  image: quay.io/keycloak/keycloak:25.0
  container_name: keycloak-sso
  entrypoint:
    - /bin/bash
    - -lc
  command:
    - /opt/keycloak/bin/kc.sh import --file=/opt/keycloak/data/import/realm-export-extras.json --override true && /opt/keycloak/bin/kc.sh import --file=/opt/keycloak/data/import/realm-export.json --override true && exec /opt/keycloak/bin/kc.sh start-dev
  environment:
    KC_DB: postgres
    KC_DB_URL_HOST: keycloak-postgres
    KC_DB_URL_DATABASE: keycloak
    KC_DB_USERNAME: keycloak
    KC_DB_PASSWORD: keycloak
    KEYCLOAK_ADMIN: admin
    KEYCLOAK_ADMIN_PASSWORD: admin
  volumes:
    - ../el-almacen-de-peliculas-online-keycloak/docker/keycloak/realm-export.json:/opt/keycloak/data/import/realm-export.json:ro
    - ../el-almacen-de-peliculas-online-keycloak/docker/keycloak/realm-export-extras.json:/opt/keycloak/data/import/realm-export-extras.json:ro
```

### Runtime real del contenedor (`docker inspect`)

Comando ejecutado:

```powershell
docker inspect keycloak-sso --format "Name={{.Name}}`nImage={{.Config.Image}}`nEntrypoint={{json .Config.Entrypoint}}`nCmd={{json .Config.Cmd}}`nEnv={{json .Config.Env}}`nMounts={{json .Mounts}}"
```

Salida relevante:

- `Image=quay.io/keycloak/keycloak:25.0`
- `Entrypoint=["/bin/bash","-lc"]`
- `Cmd=["/opt/keycloak/bin/kc.sh import --file=/opt/keycloak/data/import/realm-export-extras.json --override true && /opt/keycloak/bin/kc.sh import --file=/opt/keycloak/data/import/realm-export.json --override true && exec /opt/keycloak/bin/kc.sh start-dev"]`
- Mounts bind (RO):
  - `.../realm-export.json -> /opt/keycloak/data/import/realm-export.json`
  - `.../realm-export-extras.json -> /opt/keycloak/data/import/realm-export-extras.json`

## 3. Evidence of import execution (logs)

Comando ejecutado:

```powershell
docker logs keycloak-sso --tail 500 | Select-String -Pattern "import|Realm|WARN|ERROR|KC-SERVICES0030|KC-SERVICES0032" -CaseSensitive:$false
```

Líneas clave observadas:

- `Full importing from file /opt/keycloak/data/import/realm-export-extras.json`
- `Realm 'videoclub' already exists. Removing it before import`
- `Realm 'videoclub' imported`
- `Full importing from file /opt/keycloak/data/import/realm-export.json`
- `Realm 'videoclub' already exists. Removing it before import`
- `Realm 'videoclub' imported`

También aparecen errores no bloqueantes de listener:

- `KC-SERVICES0083: Event listener 'keycloak-to-rabbitmq' registered, but provider not found`

No se observaron errores que indiquen fallo del import de realm.

## 4. Evidence of import inputs (files in container + hashes)

Comandos ejecutados:

```powershell
docker exec keycloak-sso sh -lc "ls -l /opt/keycloak/data/import"
docker exec keycloak-sso sh -lc "sha256sum /opt/keycloak/data/import/realm-export.json /opt/keycloak/data/import/realm-export-extras.json"
docker exec keycloak-sso sh -lc "ls -la /opt/keycloak/data"
```

Salida relevante:

- `/opt/keycloak/data/import/realm-export.json` (87164 bytes, readable)
- `/opt/keycloak/data/import/realm-export-extras.json` (86098 bytes, readable)
- SHA256:
  - `realm-export.json`: `e9c1ee9be30a1aed8ce5094603177b54ba64c42893f5f2122264e0620647c5ea`
  - `realm-export-extras.json`: `276cc4b7e76a52c8694330f7e02bad3ae929a2b585eec583490c8fc5a916d649`

## 5. Resulting Keycloak state (Admin API findings)

Comandos ejecutados (read-only):

```powershell
# token admin-cli
POST /realms/master/protocol/openid-connect/token
# realm
GET /admin/realms/videoclub
# user search
GET /admin/realms/videoclub/users?username=usuarioadmin
# clients
GET /admin/realms/videoclub/clients
GET /admin/realms/videoclub/clients/{id}
# auth flows + required actions
GET /admin/realms/videoclub/authentication/flows
GET /admin/realms/videoclub/authentication/required-actions
```

Estado observado:

- Realm `videoclub` existe y está habilitado (`enabled=true`).
- `registrationAllowed=false`.
- `resetPasswordAllowed=false`.
- `browserFlow=browser`, `registrationFlow=registration`.
- Usuario `usuarioadmin` presente (`count=1`, `enabled=true`, sin requiredActions).
- Cliente `web` presente con redirect URIs esperadas (incluye `http://localhost:5173/*`).
- Cliente `test-api` presente con configuración coincidente.
- Flow `registration` existe (`topLevel=true`, `builtIn=true`).

## 6. Gap analysis: JSON vs actual (focus: registration button)

Comparación de exports (lectura directa de JSON en host):

- `realm-export-extras.json`:
  - `registrationAllowed=true`
  - `resetPasswordAllowed=true`
  - **no** contiene usuario `usuarioadmin`
- `realm-export.json`:
  - `registrationAllowed=false`
  - `resetPasswordAllowed=false`
  - **sí** contiene usuario `usuarioadmin`

Runtime actual de Keycloak:

- `registrationAllowed=false`
- `resetPasswordAllowed=false`
- `usuarioadmin` existe

Conclusión del gap:

- El estado final coincide con `realm-export.json` (último import), no con `realm-export-extras.json`.
- Por eso el botón de registro no aparece: el realm terminó con `registrationAllowed=false`.

## 7. Most likely root cause hypotheses (ranked, evidence-backed)

1. **Orden de import + override (más probable)**
   - Evidencia: comando runtime importa primero `realm-export-extras.json` y luego `realm-export.json`, ambos con `--override true`.
   - Efecto: el segundo import sobrescribe configuración del primero; “gana” `registrationAllowed=false`.

2. **Expectativa funcional conflictiva entre archivos JSON**
   - Evidencia: `extras` declara registro habilitado, `main` lo deshabilita.
   - Efecto: comportamiento final percibido como “import parcial”, cuando en realidad es sobrescritura coherente con el orden.

3. **Ruido operativo por listener faltante (secundario, no causal del síntoma)**
   - Evidencia: `KC-SERVICES0083` por `keycloak-to-rabbitmq` provider missing.
   - Impacto observado: no bloquea import ni startup, pero agrega errores en logs.

## 8. Next safe steps (read-only suggestions only)

- Confirmar con el equipo cuál archivo debe prevalecer para settings de realm (`main` vs `extras`).
- Mantener una matriz de campos críticos por archivo (ej.: `registrationAllowed`, `resetPasswordAllowed`, temas, required actions) para evitar expectativas cruzadas.
- En futuras auditorías read-only, verificar siempre: command real (`docker inspect`) + logs de import + Admin API final.
- Si se requiere rastreo histórico sin cambios, conservar snapshots de:
  - `docker inspect keycloak-sso`
  - líneas de import en logs
  - export GET de `/admin/realms/videoclub`.
