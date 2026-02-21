# API CONTRACT CATALOGO — Step 2.2 Validations

## 1) Reglas reforzadas

Se endureció la validación de parámetros de paginación y orden para ambos endpoints canónicos:

- Público: `GET /peliculas`
- Admin: `GET /api/admin/peliculas`

Reglas aplicadas:

- `page` debe ser `>= 0`
- `size` debe estar entre `1` y `MAX_SIZE`
- `MAX_SIZE = 100`
- `sort` debe pertenecer al allowlist explícito:
  - `titulo`
  - `precio`
  - `fechaSalida`
  - `genero`
  - `formato`
  - `condicion`
- `asc` se mantiene boolean

La validación se implementa en un punto compartido del pipeline (`PeliculaService.buscarPaginado(...)`) para garantizar comportamiento idéntico entre público y admin.

## 2) Contrato de error determinístico (HTTP 400)

Cuando la validación falla, la API retorna HTTP 400 con shape estable:

```json
{
  "code": "INVALID_SIZE",
  "message": "El parámetro 'size' debe estar entre 1 y 100",
  "details": {
    "field": "size",
    "value": 101,
    "rule": "1..100"
  }
}
```

Códigos usados:

- `INVALID_PAGE`
- `INVALID_SIZE`
- `INVALID_SORT`

Este contrato se aplica de forma uniforme en ambos endpoints por medio de `ApiErrorHandler`.

## 3) Archivos modificados/agregados

- `src/main/java/unrn/service/PeliculaService.java`
- `src/main/java/unrn/service/CatalogoQueryValidationException.java` (nuevo)
- `src/main/java/unrn/api/ApiErrorHandler.java`
- `src/test/java/unrn/api/PeliculaPublicContractIntegrationTest.java`
- `src/test/java/unrn/api/AdminPeliculasContractIntegrationTest.java`
- `docs/bitacoras/API_CONTRACT_CATALOGO_STEP2_2_VALIDATIONS.md` (nuevo)

## 4) Tests agregados y garantías

### Público

- `listarPublico_pageNegativo_devuelve400`
- `listarPublico_sizeCero_devuelve400`
- `listarPublico_sizeExcesivo_devuelve400`
- `listarPublico_sortInvalido_devuelve400`

Garantizan:

- HTTP 400 para input inválido
- respuesta JSON de error con `code` y `message`

### Admin (JWT basado en claims)

- `listarAdmin_pageNegativo_devuelve400`
- `listarAdmin_sizeCero_devuelve400`
- `listarAdmin_sizeExcesivo_devuelve400`
- `listarAdmin_sortInvalido_devuelve400`

Garantizan:

- HTTP 400 para input inválido
- respuesta JSON de error con `code` y `message`
- cobertura del endpoint protegido con setup JWT claim-based consistente con Step 1.1

## 5) Cómo verificar

Desde el módulo backend:

```bash
mvn -Dtest=unrn.api.PeliculaPublicContractIntegrationTest,unrn.api.AdminPeliculasContractIntegrationTest test
```

Opcional (suite extendida de seguridad + contrato):

```bash
mvn -Dtest=unrn.service.AdminPeliculasSecurityIntegrationTest,unrn.api.PeliculaPublicContractIntegrationTest,unrn.api.AdminPeliculasContractIntegrationTest test
```

## 6) Confirmación de alcance

**No Docker changes were made.**
