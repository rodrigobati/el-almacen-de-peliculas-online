# Diagnóstico y Plan de Corrección — Error al agregar al carrito

## Summary

- El `400` de `POST /api/carrito/items` lo devuelve `ventas-service` (no gateway), con payload: `{"code":"COMPRA_ERROR","message":"La película no existe en la proyección"}`.
- La tabla de proyección de ventas está vacía (`pelicula_proyeccion = 0`), mientras catálogo sí tiene películas.
- RabbitMQ muestra `ventas.movie.queue` con mensaje atascado (`messages_unacknowledged=1`) y consumidor activo.
- En logs de ventas, el listener de eventos falla con `RuntimeException: La versión debe ser mayor a cero`.
- No Docker changes were made.

## Observations

- Routing del gateway correcto: `/api/carrito/** -> http://ventas-service:8083`.
- Seguridad también correcta: sin token da `401`; con token válido llega a lógica de negocio y devuelve `400`.
- Catálogo tiene datos, pero ventas no proyectó nada.
- Además, el rebuild interno de proyección también falla en este estado (respuesta inválida de catálogo para bootstrap).

## Hypotheses

1. **Principal**: eventos `MovieCreated/Updated` llegan con `version=0`; `ventas` rechaza porque `PeliculaProyeccion` exige `version > 0`.
2. **Secundaria**: el endpoint de bootstrap en ventas consume catálogo con parámetros/formato no compatibles y no puede repoblar.
3. **No causal principal**: `401` en rating healthcheck puede existir en entornos viejos, pero no explica este `400` de carrito.

## Step-by-step Diagnostics (numbered)

### 1) Baseline de contenedores y salud

```powershell
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
```

**Esperado:** `api-gateway`, `ventas-service`, `catalogo-backend`, `shared-rabbitmq`, `keycloak-sso` en `Up ... (healthy)`.

---

### 2) Confirmar routing de gateway para carrito

```powershell
curl.exe -s http://localhost:9500/actuator/gateway/routes
```

**Esperado:** entrada con `route_id: ventas-carrito`, `Paths: [/api/carrito/**]`, `uri: http://ventas-service:8083`.

---

### 3) Obtener token válido (Keycloak)

```powershell
$tokenResp = Invoke-RestMethod -Method Post `
  -Uri "http://localhost:9090/realms/videoclub/protocol/openid-connect/token" `
  -ContentType "application/x-www-form-urlencoded" `
  -Body @{ client_id='web'; grant_type='password'; username='usuariocliente'; password='usuariocliente' }

$token = $tokenResp.access_token
$token.Substring(0,25) + "..."
```

**Esperado:** token JWT no vacío.

(Usuario/cliente existentes en realm importado: `usuariocliente`, `usuarioadmin`, `client_id=web`).

---

### 4) Diferenciar seguridad vs negocio (401/403 vs 400)

```powershell
curl.exe -s -o NUL -w "NO_TOKEN:%{http_code}`n" `
  -X POST http://localhost:9500/api/carrito/items `
  -H "Content-Type: application/json" `
  -d "{\"peliculaId\":\"1\",\"cantidad\":1}"

curl.exe -s -o NUL -w "BAD_TOKEN:%{http_code}`n" `
  -X POST http://localhost:9500/api/carrito/items `
  -H "Authorization: Bearer invalid.token" `
  -H "Content-Type: application/json" `
  -d "{\"peliculaId\":\"1\",\"cantidad\":1}"
```

**Esperado:** `401` y `401`.

---

### 5) Reproducir fallo real de carrito con token

```powershell
try {
  Invoke-WebRequest -Method Post -UseBasicParsing -ErrorAction Stop `
    -Uri "http://localhost:9500/api/carrito/items" `
    -Headers @{ Authorization="Bearer $token"; "Content-Type"="application/json" } `
    -Body '{"peliculaId":"1","cantidad":1}'
} catch {
  $resp = $_.Exception.Response
  $sr = New-Object System.IO.StreamReader($resp.GetResponseStream())
  "STATUS=" + [int]$resp.StatusCode
  "BODY=" + $sr.ReadToEnd()
}
```

**Esperado:**

- `STATUS=400`
- `BODY={"code":"COMPRA_ERROR","message":"La película no existe en la proyección"}`

---

### 6) Probar directo a ventas (sin gateway) para identificar origen exacto

```powershell
try {
  Invoke-WebRequest -Method Post -UseBasicParsing -ErrorAction Stop `
    -Uri "http://localhost:8083/carrito/items" `
    -Headers @{ Authorization="Bearer $token"; "Content-Type"="application/json" } `
    -Body '{"peliculaId":"1","cantidad":1}'
} catch {
  $resp = $_.Exception.Response
  $sr = New-Object System.IO.StreamReader($resp.GetResponseStream())
  "STATUS_DIRECT=" + [int]$resp.StatusCode
  "BODY_DIRECT=" + $sr.ReadToEnd()
}
```

**Esperado:** mismo `400` y mismo body ⇒ error originado en `ventas-service`.

---

### 7) Verificar datos en DB (catálogo vs proyección ventas)

#### 7.1 Catálogo tiene películas

```powershell
docker exec -i catalogo-mysql mysql -uroot -proot -D almacen_peliculas -e "
SELECT COUNT(*) AS peliculas_catalogo FROM pelicula;
SELECT id,titulo,precio,activa,version FROM pelicula ORDER BY id LIMIT 10;"
```

**Esperado:** `peliculas_catalogo > 0`.

#### 7.2 Proyección de ventas

```powershell
docker exec -i catalogo-mysql mysql -uroot -proot -D almacen_ventas -e "
SELECT COUNT(*) AS proyeccion_total FROM pelicula_proyeccion;
SELECT movie_id,titulo,precio_actual,activa,version FROM pelicula_proyeccion LIMIT 10;"
```

**Esperado (fallando):** `proyeccion_total = 0`.

#### 7.3 Consistencia entre DBs (faltantes)

```powershell
docker exec -i catalogo-mysql mysql -uroot -proot -e "
SELECT p.id
FROM almacen_peliculas.pelicula p
LEFT JOIN almacen_ventas.pelicula_proyeccion pp
  ON pp.movie_id = CAST(p.id AS CHAR)
WHERE pp.movie_id IS NULL
LIMIT 20;"
```

**Esperado (fallando):** devuelve ids faltantes (ej. `1..`).

---

### 8) Revisar RabbitMQ (colas, backlog, consumidores)

```powershell
docker exec -i shared-rabbitmq rabbitmqctl list_queues name messages_ready messages_unacknowledged consumers
```

**Esperado (fallando):** `ventas.movie.queue` con `messages_unacknowledged > 0` y consumidor `>0`.

---

### 9) Revisar logs de ventas para causa técnica del consumidor

```powershell
docker logs ventas-service --tail 400
```

Buscar:

- `Evento de catalogo recibido: MovieCreated.v1`
- `ListenerExecutionFailedException`
- `La versión debe ser mayor a cero`

**Esperado (fallando):** excepción repetida al procesar evento de película.

---

### 10) Verificar bootstrap/rebuild de proyección (ruta de recuperación)

```powershell
curl.exe -s -i -X POST http://localhost:8083/internal/projection/rebuild `
  -H "X-Internal-Token: changeme-bootstrap-token"
```

**Esperado (actual en falla):** `400` con error de consumo/formato de respuesta catálogo.

## Minimal Fix Plan

1. **Fix mínimo principal (ventas):** aceptar `version=0` en la proyección/event handler (hoy bloquea consumo y deja cola atascada).
2. **Fix mínimo de recuperación (ventas bootstrap):** robustecer `HttpCatalogoClient` para validar status HTTP y formato de respuesta de catálogo antes de iterar `items` (si no viene página válida, lanzar error explícito y manejable).
3. **Ejecutar rebuild** de proyección tras fix y/o reinyectar eventos pendientes.
4. **No tocar Docker Compose** para resolver esta incidencia funcional.

## Verification

- `POST /api/carrito/items` con token devuelve `200` y carrito con item agregado.
- `almacen_ventas.pelicula_proyeccion` pasa de `0` a `>0`.
- `rabbitmqctl list_queues` muestra `ventas.movie.queue` sin `messages_unacknowledged`.
- Logs de ventas sin nuevas `ListenerExecutionFailedException` para `MovieEventListener`.
- Prueba de seguridad sigue correcta: sin token continúa devolviendo `401`.

## Definition of Done

- [ ] Identificado y probado el servicio origen del `400` (`ventas-service`).
- [ ] Payload exacto del error capturado y documentado.
- [ ] Proyección `pelicula_proyeccion` poblada y consistente con catálogo.
- [ ] `POST /api/carrito/items` exitoso end-to-end vía gateway.
- [ ] Sin mensajes atascados ni errores repetidos de listener en RabbitMQ/logs.
- [ ] Validado que auth sigue diferenciando `401` (seguridad) de `400` (negocio).

## If Still Failing

- Revisar si catálogo publica eventos con `version` inválida o payload incompleto.
- Verificar que la cola `ventas.movie.queue` no tenga poison messages; purgar solo como último recurso controlado.
- Confirmar conectividad/resolución DNS interna (`catalogo-backend`, `shared-rabbitmq`, `keycloak-sso`) desde `ventas-service`.
- Si aparece nuevamente el problema de healthcheck de rating por `401`, tratarlo como issue separado de observabilidad (no causal del carrito).
