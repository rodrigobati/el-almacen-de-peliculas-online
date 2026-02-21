# FIX-KEYCLOAK-IMPORT

## Root cause

- Se estaban ejecutando **dos imports secuenciales** del mismo realm (`videoclub`) con `--override true`.
- El segundo import reemplazaba el estado del primero (incluyendo configuración de realm y entidades), generando resultados no determinísticos según orden/contenido.

## Minimal fix applied

1. **Import único y consolidado** en `keycloak` dentro de `docker-compose-full.yml`:
   - Antes: import de `realm-export-extras.json` + import de `realm-export.json`.
   - Ahora: solo import de `realm-export.json` y luego `start-dev`.
2. **Consolidación mínima en** `el-almacen-de-peliculas-online-keycloak/docker/keycloak/realm-export.json`:
   - `registrationAllowed: true`
   - `resetPasswordAllowed: true`
   - `rememberMe: true`
   - `editUsernameAllowed: true`
3. **`usuarioadmin` se mantiene** en el export principal con credencial password `usuarioadmin` y `temporary=false`.
4. No se agregó ningún servicio de bootstrap ni pasos manuales en UI.

## About `keycloak-bootstrap`

- En `docker-compose-full.yml` **no existe** servicio `keycloak-bootstrap` activo.
- Tampoco hay imports dependientes de scripts externos para provisión de realm.

## Validation commands

1. Recrear Keycloak y su DB:

```powershell
docker compose -f docker-compose-full.yml up -d --force-recreate keycloak-postgres keycloak
```

Esperado:

- `keycloak-postgres` healthy
- `keycloak-sso` started

2. Verificar logs de import (single import):

```powershell
docker logs keycloak-sso --tail 200
```

Esperado:

- Aparece `Full importing from file /opt/keycloak/data/import/realm-export.json`
- **No** aparece segundo import de `realm-export-extras.json`

3. Admin API (read-only):

```powershell
# token
POST http://localhost:9090/realms/master/protocol/openid-connect/token

# realm
GET http://localhost:9090/admin/realms/videoclub

# user
GET http://localhost:9090/admin/realms/videoclub/users?username=usuarioadmin
```

Esperado:

- `registrationAllowed=true`
- `usuarioadmin` presente y habilitado

4. UI check:

```text
Abrir login del realm videoclub y verificar que aparece “Register”.
```

Esperado:

- Botón/link de registro visible.
