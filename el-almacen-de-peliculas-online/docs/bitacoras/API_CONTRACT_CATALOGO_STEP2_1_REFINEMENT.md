# API CONTRACT CATALOGO — Step 2.1 Refinement

## 1) Motivación del refinamiento

Step 2.1 unificó correctamente los listados público y admin sobre el contrato canónico de catálogo. Este refinamiento agrega precisión arquitectónica para evitar ambigüedades futuras en tres puntos:

- regla determinística de `totalPages` cuando no hay resultados,
- ejemplos de URL consistentes con gateway (`API_BASE=http://localhost:9500/api`),
- definición formal de “parity” (paridad de contrato, no equivalencia de negocio).

## 2) Regla explícita de `totalPages` (decisión canónica)

Se adopta **Option A (Spring default style)**:

- Si `total == 0` -> `totalPages == 0`
- Si `total > 0` -> `totalPages = ceil(total / size)`

Justificación:

- mantiene consistencia con la semántica estándar de paginación de Spring (`Page#getTotalPages()`),
- evita valores artificiales cuando no existen elementos,
- reduce ambigüedad para clientes al representar explícitamente un catálogo vacío.

Enforcement aplicado:

- `PageResponse.of(...)` deja explícita la regla para `total == 0`.
- Ambos endpoints (`/peliculas` y `/api/admin/peliculas`) delegan en `PageResponse.of(...)` y por lo tanto comparten el mismo comportamiento.
- Tests de contrato de público y admin verifican explícitamente `totalPages == 0` en escenario sin resultados.

## 3) URLs canónicas finales (gateway-correct)

Con `API_BASE = http://localhost:9500/api`, las URLs correctas son:

- Público: `http://localhost:9500/api/peliculas`
- Admin: `http://localhost:9500/api/admin/peliculas`

Se corrigieron ejemplos para eliminar rutas inconsistentes sin prefijo `/api`.

## 4) Definición de “parity”

Paridad entre público y admin significa:

- mismos nombres de query params y mismos defaults,
- mismo shape de respuesta (`items`, `total`, `totalPages`, `page`, `size`),
- misma semántica de paging y sorting.

Paridad **no** implica:

- mismo filtrado de entidades inactivas,
- mismas reglas de visibilidad de negocio,
- igualdad de datasets de respuesta entre público y admin.

## 5) Tests actualizados y garantías

### `src/test/java/unrn/api/PeliculaPublicContractIntegrationTest.java`

- `listarPublico_devuelvePageResponseConCamposObligatorios`
  - Garantiza HTTP 200 + shape canónico.
- `listarPublico_sinResultados_devuelveTotalPagesEnCero`
  - Garantiza regla explícita `total=0 => totalPages=0`.
- `listarPublico_yAdmin_aceptanMismosParams_basicos`
  - Garantiza paridad contractual de params + shape (sin comparar resultados de negocio).

### `src/test/java/unrn/api/AdminPeliculasContractIntegrationTest.java`

- `listarAdmin_devuelvePageResponseConCamposObligatorios`
  - Garantiza HTTP 200 + shape canónico.
- `listarAdmin_sinResultados_devuelveTotalPagesEnCero`
  - Garantiza regla explícita `total=0 => totalPages=0`.
- `listarAdmin_conSortTituloDesc_respetaOrden`
  - Garantiza semántica de sorting (`sort=titulo`, `asc=false`).

## 6) Confirmación de alcance

**No Docker changes were made.**
