# Docker Compose Incident Resolution - El Almacen de Peliculas Online

## Diagnosis summary

- Most likely cause: stale Compose state vs. container lifecycle mismatch.
- Evidence: `docker compose config --services` includes `catalogo-backend` and `docker ps -a` shows a `catalogo-backend` container, but it is in a restart loop. Docker Desktop shows "not found" when trying to start because the Compose app tries to start a service that is failing or in a broken state, not because the service is missing in config.

## Exact command sequence (copy/paste)

```powershell
Set-Location "C:\Users\pelud\OneDrive\Documentos\UNRN\Taller de Tecnologias y Produccion de Software\el-almacen-de-peliculas-online"

# Confirm project and services
Docker compose -f docker-compose-full.yml ls
Docker compose -f docker-compose-full.yml config --services
Docker compose -f docker-compose-full.yml ps
Docker ps -a --filter "name=catalogo-backend"

# Inspect the failing container
Docker compose -f docker-compose-full.yml logs -f --tail=200 catalogo-backend

# Safe cleanup (no volumes removed)
Docker compose -f docker-compose-full.yml down --remove-orphans

# Rebuild and start
Docker compose -f docker-compose-full.yml up -d --build

# Verify
Docker compose -f docker-compose-full.yml ps
Docker compose -f docker-compose-full.yml logs -f --tail=200 catalogo-backend
```

## Branching fixes (A/B/C)

- A) If `docker compose config --services` does NOT list `catalogo-backend`:
  - The compose file no longer defines it. Update Docker Desktop project to use the correct compose file (here, `docker-compose-full.yml`) or fix the service name in the file, then run `docker compose up -d --build`.
- B) If it DOES list `catalogo-backend` but `docker ps -a` shows no such container:
  - Compose state is stale. Run:
    - `docker compose down --remove-orphans`
    - `docker compose up -d`
- C) If container exists but will not start:
  - Review `docker compose logs -f --tail=200 catalogo-backend` and fix the root cause (bad env vars, port conflicts, missing DB, migration failure, etc.), then `docker compose up -d --build`.

## Verification checklist

- `docker compose ps` shows all services Up (or healthy where healthchecks exist).
- Ports mapped and responding:
  - Catalog backend: `http://localhost:8081`
  - API gateway: `http://localhost:9500`
  - Keycloak: `http://localhost:9090`
  - Frontend: `http://localhost:5173` (if Vite)
- Key endpoints respond:
  - `GET /categorias` on catalog backend
  - `GET /peliculas` on catalog backend

## Do not do

- Do not delete volumes (`docker volume rm` or `docker compose down -v`) unless you explicitly want to wipe DB/Keycloak data.
- Do not rename services in the compose file without updating Docker Desktop project.
- Do not use multiple compose files without confirming the active project and config file.
