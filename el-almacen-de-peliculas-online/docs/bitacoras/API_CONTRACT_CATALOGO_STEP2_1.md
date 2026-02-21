# API CONTRACT CATALOGO — Step 2.1

## 1) Síntoma/Riesgo abordado

Riesgo de divergencia de contrato entre listados de catálogo público y admin:

- Diferencias de parámetros de paging/sorting o defaults.
- Diferencias de shape de respuesta (PageResponse vs arrays planos).
- Riesgo de regresión en frontend y clientes API al consumir rutas distintas para el mismo concepto.

Objetivo de esta etapa: forzar contrato único y verificable por tests para `GET /peliculas` y `GET /api/admin/peliculas`.

## 2) Definición del contrato canónico

### Endpoints

- Público: `GET /peliculas`
- Admin: `GET /api/admin/peliculas`

### Query params canónicos (idénticos en ambos)

- `q` (opcional)
- filtros existentes: `genero`, `formato`, `condicion`, `actor`, `director`, `minPrecio`, `maxPrecio`, `desde`, `hasta`
- `page` (default `0`)
- `size` (default `12`)
- `sort` (default `titulo`)
- `asc` (default `true`)

### Shape canónico de respuesta

Ambos endpoints devuelven siempre `PageResponse<DetallePeliculaDTO>`:

- `items` (array)
- `total` (number)
- `totalPages` (number)
- `page` (number)
- `size` (number)

### Regla explícita de `totalPages`

Se adopta regla canónica **Option A (Spring default style)**:

- Si `total == 0` -> `totalPages == 0`.
- Si `total > 0` -> `totalPages = ceil(total / size)`.

Esta regla se centraliza en `PageResponse.of(...)` y aplica igual para público y admin.

### Definición explícita de paridad público/admin

Paridad entre ambos endpoints significa:

- mismos nombres de query params y mismos defaults,
- mismo shape de respuesta,
- misma semántica de paging/sorting.

Paridad **no** implica:

- mismas reglas de visibilidad de negocio,
- mismo filtrado de entidades inactivas,
- igualdad de resultados funcionales en todos los contextos.

## 3) Cambios realizados (controllers/service)

### Servicio como fuente única para listado + DTO paginado

Se agregó en `PeliculaService` el método:

- `buscarPaginadoDetalle(...)` -> delega en `buscarPaginado(...)` y mapea a `DetallePeliculaDTO`.

Con esto, público y admin comparten exactamente el mismo pipeline de:

1. filtros,
2. paging,
3. sorting (`sort` + `asc`),
4. mapeo a DTO.

### Controladores alineados al mismo método

Se actualizaron:

- `unrn.api.PeliculaController`
- `unrn.api.PeliculaAdminController`

Ambos:

- aceptan el mismo set de parámetros y defaults,
- llaman al mismo método de servicio (`buscarPaginadoDetalle`),
- construyen respuesta con `PageResponse.of(...)`.

No queda ningún camino de retorno de `List<>` para listado admin de películas.

## 4) Tests agregados/actualizados

### Nuevos tests de contrato

Archivo: `src/test/java/unrn/api/PeliculaPublicContractIntegrationTest.java`

- `listarPublico_devuelvePageResponseConCamposObligatorios`
  - Garantiza shape canónico + `page/size` esperados.
- `listarPublico_sinResultados_devuelveTotalPagesEnCero`
  - Garantiza regla explícita: cuando `total=0`, `totalPages=0`.
- `listarPublico_yAdmin_aceptanMismosParams_basicos`
  - Garantiza paridad de params (`q,page,size,sort,asc`) y shape en ambos endpoints.

Archivo: `src/test/java/unrn/api/AdminPeliculasContractIntegrationTest.java`

- `listarAdmin_devuelvePageResponseConCamposObligatorios`
  - Garantiza shape canónico + `page/size` esperados.
- `listarAdmin_sinResultados_devuelveTotalPagesEnCero`
  - Garantiza regla explícita: cuando `total=0`, `totalPages=0`.
- `listarAdmin_conSortTituloDesc_respetaOrden`
  - Garantiza orden por `sort=titulo&asc=false` (`ZZZ` primero).

Los tests de paridad validan únicamente HTTP 200, shape y semántica de paging/sorting.
No comparan datasets completos entre público y admin.

### Seguridad claim-based en admin tests

Los requests admin se ejecutan con `spring-security-test` + JWT claims (`realm_access`, `resource_access`, `azp`) y authorities derivadas por `JwtRoleConverter` para mantener consistencia con Step 1.1.

## 5) Cómo verificar

### Tests focalizados

Desde módulo backend (`el-almacen-de-peliculas-online/el-almacen-de-peliculas-online`):

```bash
mvn -Dtest=unrn.api.PeliculaPublicContractIntegrationTest,unrn.api.AdminPeliculasContractIntegrationTest test
```

Opcional corrida extendida de seguridad+contrato:

```bash
mvn -Dtest=unrn.service.AdminPeliculasSecurityIntegrationTest,unrn.api.PeliculaPublicContractIntegrationTest,unrn.api.AdminPeliculasContractIntegrationTest test
```

### Ejemplos de consumo

Público:

```bash
curl -s "http://localhost:9500/api/peliculas?q=matrix&page=0&size=2&sort=titulo&asc=true"
```

Admin:

```bash
curl -s -H "Authorization: Bearer <token_admin>" "http://localhost:9500/api/admin/peliculas?q=matrix&page=0&size=2&sort=titulo&asc=true"
```

Shape esperado en ambos:

```json
{
  "items": [],
  "total": 0,
  "totalPages": 0,
  "page": 0,
  "size": 2
}
```

## 6) Restricción de infraestructura

**No Docker changes were made.**
