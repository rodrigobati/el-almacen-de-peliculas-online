# Categorias 404 Diagnosis Report

## 1) Summary

The catalog backend exposes `/categorias` and returns 200 directly, but the API Gateway returns 404 for both `/api/categorias` and `/categorias`. The gateway is configured via environment variables in Docker Compose that define routes for `/api/peliculas`, `/api/ratings`, `/api/ventas`, and Keycloak only, so `/api/categorias` is missing at runtime even though application-docker.yml includes a categorias route.

## 2) Symptoms

- Frontend request: `GET http://localhost:9500/api/categorias` -> 404 Not Found.
- Movies list works through gateway; categories do not.

## 3) Reproduction (curl + browser)

A) Direct backend (8081)

- `curl.exe -i http://localhost:8081/categorias` -> 200 OK, JSON list of categories.
- `curl.exe -i "http://localhost:8081/peliculas?page=0&size=1"` -> 200 OK, JSON page of movies.

B) Through gateway (9500)

- `curl.exe -i http://localhost:9500/api/categorias` -> 404 Not Found.
- `curl.exe -i http://localhost:9500/categorias` -> 404 Not Found.
- `curl.exe -i "http://localhost:9500/api/peliculas?page=0&size=1"` -> 200 OK.
- `curl.exe -i "http://localhost:9500/peliculas?page=0&size=1"` -> 404 Not Found.

## 4) Evidence

### 4.1 Direct backend calls

- `/categorias` exists and responds 200 on the catalog service (8081).
- `/peliculas` exists and responds 200 on the catalog service (8081).

### 4.2 Gateway calls

- `/api/peliculas` routes correctly (200), so gateway is reachable and routing works.
- `/api/categorias` is 404, so there is no matching gateway route.

### 4.3 Gateway route configuration (code)

- application-docker.yml includes a categorias route:
  - [apigateway-main/src/main/resources/application-docker.yml](apigateway-main/src/main/resources/application-docker.yml)
    - `Path=/api/categorias/**`
    - `uri: http://catalogo-backend:8080`
    - `StripPrefix=1`

- application.yml does NOT include categorias route (only peliculas):
  - [apigateway-main/src/main/resources/application.yml](apigateway-main/src/main/resources/application.yml)

### 4.4 Gateway route configuration (runtime via compose)

In Docker Compose, the API Gateway routes are injected via environment variables and do not include categorias.

- [el-almacen-de-peliculas-online/docker-compose-full.yml](el-almacen-de-peliculas-online/docker-compose-full.yml)
  - `SPRING_CLOUD_GATEWAY_ROUTES_0_PREDICATES_0: Path=/api/peliculas/**`
  - `SPRING_CLOUD_GATEWAY_ROUTES_1_PREDICATES_0: Path=/api/ratings/**`
  - `SPRING_CLOUD_GATEWAY_ROUTES_3_PREDICATES_0: Path=/api/ventas/**`
  - No `categorias` route is defined in env.

## 5) Root Cause

The gateway runtime configuration (environment variables in docker-compose-full.yml) overrides the application-docker.yml routes and omits the `/api/categorias` route. As a result, the gateway has no route for categories, causing 404 for `/api/categorias` and `/categorias`, while `/api/peliculas` works.

## 6) Fix Plan (minimal, no changes yet)

Choose one minimal approach:

Option A (preferred): Add categorias route to the gateway environment variables in docker-compose-full.yml so the runtime config includes it.

- Add route:
  - `Path=/api/categorias/**`
  - `uri: http://catalogo-backend:8080`
  - `StripPrefix=1`

Option B: Remove the gateway route env vars from docker-compose-full.yml and rely on application-docker.yml, which already defines categorias.

- Ensure `SPRING_PROFILES_ACTIVE=docker` remains so application-docker.yml is used.

Option C: Change frontend base URL to `/categorias` (no `/api`) and add a gateway route without prefix.

- Not recommended because `/api` is already the standard for peliculas and ratings.

## 7) Verification Checklist

After applying the fix:

- `curl.exe -i http://localhost:9500/api/categorias` -> 200 OK.
- `curl.exe -i "http://localhost:9500/api/peliculas?page=0&size=1"` -> 200 OK.
- Frontend loads categories in the UI without login.
- Gateway still routes ratings and ventas correctly.
