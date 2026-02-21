# RUNTIME_BITACORA_2026-02-18_23-25

Fecha/hora de cierre: 2026-02-18 23:25:57

## 1. Initial State

- Estado Docker inicial: servicios levantados y `ventas-service` en `healthy`.
- Error observado en runtime al intentar bootstrap:
  - `POST /internal/projection/rebuild` con token válido devolvía:
  - `{"code":"COMPRA_ERROR","message":"No se pudo consumir catálogo para bootstrap de proyección"}` (HTTP 400).
- Conteos antes de rebuild exitoso:
  - `CATALOGO_TOTAL` (API catálogo): `13`
  - `PROYECCION_TOTAL_ANTES` (SQL en `almacen_ventas.pelicula_proyeccion`): `1`
- Log relevante de arranque:
  - `Datasource URL: jdbc:mysql://catalogo-mysql:3306/almacen_ventas...`
  - `Flyway current version: 3`
  - `Flyway pending migrations count: 0`

## 2. Changes Applied

Archivos modificados (cambios mínimos y pragmáticos):

1. `el-almacen-de-peliculas-online-ventas/src/main/java/unrn/security/SecurityConfig.java`
   - Se habilitó `permitAll()` **solo** para `/internal/projection/rebuild` en perfil productivo-like.
   - Motivo: permitir ejecución operativa del bootstrap por token interno sin requerir JWT para ese endpoint técnico.

2. `el-almacen-de-peliculas-online-ventas/src/main/java/unrn/api/ProjectionBootstrapController.java`
   - Seguridad de token interna ajustada:
     - faltante/vacío => `401`
     - inválido => `403`
     - no configurado en servidor => `403`
   - Se agregó import faltante de `ApiErrorResponse` (error real de compilación en build Docker).

3. `el-almacen-de-peliculas-online-ventas/src/main/java/unrn/service/HttpCatalogoClient.java`
   - Se hizo tolerante a campos extra del catálogo (`@JsonIgnoreProperties(ignoreUnknown=true)`).
   - Se corrigió mapeo de `activa` ausente en payload de catálogo:
     - si no viene, default `true`.
   - Motivo: evitar fallo de deserialización y evitar reconstrucción de proyección en estado inactivo por defecto.

## 3. Docker Actions Executed

Comandos ejecutados:

```powershell
Set-Location "c:\Users\pelud\OneDrive\Documentos\UNRN\Taller de Tecnologías y Producción de Software\el-almacen-de-peliculas-online"
docker compose -f docker-compose-full.yml build ventas-service
docker compose -f docker-compose-full.yml up -d --no-deps --force-recreate ventas-service
docker compose -f docker-compose-full.yml ps ventas-service
```

Estado posterior al recreate:

- `ventas-service` => `Up ... (healthy)`
- Puerto publicado: `0.0.0.0:8083->8083/tcp`

## 4. Bootstrap Execution

Comandos HTTP ejecutados:

```bash
# sin token
curl -X POST "http://localhost:8083/internal/projection/rebuild"

# token inválido
curl -X POST "http://localhost:8083/internal/projection/rebuild" -H "X-Internal-Token: invalido"

# token válido (runtime actual)
curl -X POST "http://localhost:8083/internal/projection/rebuild" -H "X-Internal-Token: changeme-bootstrap-token"
```

Respuestas:

- Sin token: `HTTP 401`
  - `{"code":"PROJECTION_BOOTSTRAP_UNAUTHORIZED","message":"Token interno inválido"}`
- Token inválido: `HTTP 403`
  - `{"code":"PROJECTION_BOOTSTRAP_FORBIDDEN","message":"Token interno inválido"}`
- Token válido: `HTTP 200`
  - `{"fetched":13,"inserted":0,"updated":13,"deactivated":0,"durationMs":221}`

Resumen de logs:

- `projection-bootstrap-summary fetched=13, inserted=0, updated=13, deactivated=0, durationMs=221`

## 5. Database Verification

SQL ejecutado para comparar catálogo vs proyección:

```sql
SELECT COUNT(*) AS total_catalogo_activo
FROM almacen_peliculas.pelicula
WHERE activa = b'1';

SELECT COUNT(*) AS total_proyeccion
FROM almacen_ventas.pelicula_proyeccion;

SELECT COUNT(*) AS faltantes_en_proyeccion
FROM almacen_peliculas.pelicula p
LEFT JOIN almacen_ventas.pelicula_proyeccion pp
  ON pp.movie_id = CAST(p.id AS CHAR)
WHERE p.activa = b'1'
  AND pp.movie_id IS NULL;

SELECT COUNT(*) AS sobrantes_en_proyeccion
FROM almacen_ventas.pelicula_proyeccion pp
LEFT JOIN almacen_peliculas.pelicula p
  ON CAST(p.id AS CHAR)=pp.movie_id
WHERE p.id IS NULL;
```

Resultados:

- `total_catalogo_activo = 13`
- `total_proyeccion = 13`
- `faltantes_en_proyeccion = 0`
- `sobrantes_en_proyeccion = 0`

Sample rows (`almacen_ventas.pelicula_proyeccion`):

- `1 | Blade Runner | 9999.99 | 1 | 1`
- `17 | Event Horizon | 5000.00 | 1 | 1`
- `18 | El señor de los anillos | 6000.00 | 1 | 1`
- `19 | Alien | 9000.00 | 1 | 1`
- `20 | Spawn | 4000.00 | 1 | 1`

## 6. Functional Verification

Evidencia HTTP real de `POST /api/carrito/items` con múltiples IDs (vía API Gateway + JWT):

1. Se obtuvo token en Keycloak:

```http
POST http://localhost:9090/realms/videoclub/protocol/openid-connect/token
Body: client_id=admin-cli&grant_type=password&username=usuariocliente&password=usuariocliente
```

- Resultado: `TOKEN_OBTENIDO=True`

2. Se agregaron 3 películas distintas:

- `movieId=19` => carrito OK, total `9000.00`
- `movieId=1` => carrito OK, total `18999.99`
- `movieId=32` => carrito OK, total `19000.99`

Carrito final:

- items: `19`, `1`, `32`
- total: `19000.99`

3. Ausencia de errores SQL en logs

Se revisaron logs de `ventas-service` (tail 300) buscando:

- `bad SQL grammar`
- `SQLSyntaxErrorException`
- `doesn't exist`
- `COMPRA_ERROR`

Resultado:

- No se observaron errores SQL.
- Solo se observó el resumen correcto de bootstrap.

## 7. Final State Confirmation

- La proyección quedó completa y consistente con catálogo activo (`13 vs 13`, sin faltantes/sobrantes).
- El endpoint interno de bootstrap funciona en runtime con seguridad simple y real (`401/403/200`).
- Se previene ejecución simultánea por lock y se mantiene liberación en `finally`.
- El flujo funcional de carrito quedó operativo end-to-end para múltiples movie IDs en entorno Docker.
- No se modificó Docker Compose.
