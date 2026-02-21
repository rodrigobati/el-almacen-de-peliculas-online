# RUNTIME_BITACORA - Diagnóstico end-to-end catálogo → ventas (proyección)

Fecha: 2026-02-19

## Objetivo

Diagnosticar y corregir por qué en `Agregar al carrito` aparecía:

`La película no existe en la proyección`

con foco en el flujo de eventos:

`catalogo (publisher) -> RabbitMQ -> ventas (listener) -> pelicula_proyeccion`.

---

## Causa raíz confirmada

Se encontraron **dos desalineaciones reales** en runtime:

1. **Exchange incorrecto en catálogo para eventos de película**
   - `MovieEventPublisher` publicaba en el bean `exchangeVideoCloub00` (tomaba `rabbitmq.event.exchange.name`).
   - En properties estaba configurado `exchange_videocloud00`.
   - Ventas consume desde `catalogo.events`.

2. **Routing key binding no compatible en ventas**
   - Ventas escuchaba con binding `Movie.#`.
   - Catálogo publica `MovieCreated.v1`, `MovieUpdated.v1`, `MovieRetired.v1`.
   - En un topic exchange, `Movie.#` matchea `Movie.<algo>`, **no** `MovieUpdated.v1`.

Resultado: los eventos de catálogo no llegaban de forma consistente a `ventas.movie.queue`, por eso `pelicula_proyeccion` quedaba vacía o desactualizada.

---

## Cambios aplicados

### 1) Catálogo: exchange alineado a `catalogo.events`

Archivo:

- `el-almacen-de-peliculas-online/src/main/resources/application.properties`
- `el-almacen-de-peliculas-online/src/main/resources/application-docker.properties`

Cambio:

- `rabbitmq.event.exchange.name=exchange_videocloud00`
- a `rabbitmq.event.exchange.name=catalogo.events`

### 2) Ventas: binding explícito a routing keys reales

Archivo:

- `el-almacen-de-peliculas-online-ventas/src/main/java/unrn/event/movie/MovieEventListener.java`

Cambio:

- De key dinámica `Movie.#`
- A keys explícitas:
  - `MovieCreated.v1`
  - `MovieUpdated.v1`
  - `MovieRetired.v1`

---

## Verificación runtime ejecutada

### A. Rebuild/recreate de servicios

Se ejecutó:

- `docker compose -f docker-compose-full.yml up -d --build catalogo-backend`
- `docker compose -f docker-compose-full.yml up -d --build ventas-service`

### B. Evidencia de datos base (antes)

Consulta SQL:

```sql
SELECT COUNT(*) FROM almacen_peliculas.pelicula;
SELECT COUNT(*) FROM almacen_ventas.pelicula_proyeccion;
```

Resultado:

- catálogo tenía películas (23)
- `pelicula_proyeccion` estaba vacía (0)

### C. Evidencia de bindings en RabbitMQ (después del fix)

Se verificaron bindings efectivos:

- `catalogo.events -> ventas.movie.queue : MovieCreated.v1`
- `catalogo.events -> ventas.movie.queue : MovieUpdated.v1`
- `catalogo.events -> ventas.movie.queue : MovieRetired.v1`

### D. Prueba de enrutamiento/evento

Se publicó evento de diagnóstico en `catalogo.events` con routing key `MovieUpdated.v1` y envelope de película.

Resultado RabbitMQ:

- `"routed": true`

### E. Evidencia de proyección actualizada

Consulta SQL:

```sql
SELECT movie_id,titulo,precio_actual,activa,version
FROM almacen_ventas.pelicula_proyeccion
WHERE movie_id='1';
```

Resultado:

- `1 | Blade Runner | 9999.99 | 1 | 1`

Esto confirma que el flujo `evento -> listener -> upsert` funciona tras la corrección.

---

## Nota sobre logs de error observados

Durante diagnóstico aparecieron errores `El payload no puede ser nulo` en `ventas` causados por mensajes de prueba previos mal formados (sin type headers esperados). No corresponden al flujo normal de catálogo tras el fix.

---

## Estado final

- Root cause identificado y corregido.
- `pelicula_proyeccion` se puebla correctamente al recibir evento válido de catálogo.
- El error funcional original queda explicado por desalineación de exchange + routing key.

## No Docker changes were made.
