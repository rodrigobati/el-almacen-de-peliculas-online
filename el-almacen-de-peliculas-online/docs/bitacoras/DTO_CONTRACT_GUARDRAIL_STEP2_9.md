````markdown
# DTO Contract Guardrail — Step 2.9

Date: 2026-02-20

## Objetivo

Proteger el esquema JSON del DTO canónico usado en los listados paginados (`DetallePeliculaDTO`) para detectar cambios breaking en tiempo de CI.

## Decisión

- Enforce presence + JSON node types for a conservative set of mandatory fields. Extra fields are allowed.
- Use the real `ObjectMapper` from the application context to ensure serialization matches runtime.

## Archivos añadidos

- `src/test/java/unrn/api/DtoContractGuardrailTest.java` — prueba que serializa un `DetallePeliculaDTO` de ejemplo y verifica presencia y tipos de campos.
- `docs/bitacoras/DTO_CONTRACT_GUARDRAIL_STEP2_9.md` — este documento.

## Campos obligatorios verificados

- `id` — number
- `titulo` — string
- `precio` — number
- `directores` — array
- `actores` — array
- `fechaSalida` — textual ISO date or numeric timestamp (tolerant)
- `rating` — number

Nota: `ratingPromedio` y `totalRatings` son verificados por tipo solo si están presentes (pueden ser nulos).

## Test agregado (resumen)

- `DetallePeliculaDTO_serialization_mandatoryFieldsAndTypes` in `DtoContractGuardrailTest`:
  - Construye un `DetallePeliculaDTO` con valores de ejemplo.
  - Serializa con el `ObjectMapper` del contexto y valida `JsonNode` presence/types.

## Cómo ejecutar (comandos)

Run only Step 2.9 test:

```bash
mvn -f el-almacen-de-peliculas-online/el-almacen-de-peliculas-online/pom.xml test -Dtest=unrn.api.DtoContractGuardrailTest
```
````

Run Step 2.8, 2.8.1 and 2.9 together:

```bash
mvn -f el-almacen-de-peliculas-online/el-almacen-de-peliculas-online/pom.xml test -Dtest=unrn.api.ResponseContractGuardrailTest,unrn.api.ValidationErrorContractIntegrationTest,unrn.api.DtoContractGuardrailTest
```

## Limitaciones

- Valida sólo esquema (presencia/tipo). No valida reglas de negocio ni valores concretos.
- Permite campos adicionales para no romper por adiciones backward-compatible.
- Depende del `ObjectMapper` del contexto; cambios en la configuración de Jackson pueden afectar las aserciones (la prueba es deliberadamente tolerante en formatos de fecha).

## Confirmación

No se modificaron archivos Docker/Compose.

---

Prepared by: Senior Platform Engineer (Contract Governance)

```

```

## Step 2.9.1 — Migration to @JsonTest

Reason for migration:

- The original DTO guardrail either started a full Spring context via `@SpringBootTest` (which pulled in web and security wiring) or relied on a manually-created `ObjectMapper`. Both options were problematic: the first triggered security beans and attempted JWKS/Keycloak lookups during test startup; the latter risked diverging from the runtime `ObjectMapper` configuration. To get the best of both worlds — the real auto-configured mapper without web/security initialization — we migrated the test to a JSON slice.

What changed:

- `DtoContractGuardrailTest` now uses `@JsonTest` with the `spring.autoconfigure.exclude` property to prevent security/web auto-config. The test autowires the Spring Boot `ObjectMapper`, so serialization matches runtime configuration.
- No production code or runtime configuration was changed.

How to run (commands):

Run only the DTO guardrail:

```bash
mvn -f el-almacen-de-peliculas-online/el-almacen-de-peliculas-online/pom.xml test -Dtest=unrn.api.DtoContractGuardrailTest
```

Run the contract suite for Steps 2.8, 2.8.1 and 2.9:

```bash
mvn -f el-almacen-de-peliculas-online/el-almacen-de-peliculas-online/pom.xml test -Dtest=unrn.api.ResponseContractGuardrailTest,unrn.api.ValidationErrorContractIntegrationTest,unrn.api.DtoContractGuardrailTest
```

Notes:

- The test asserts presence and JSON node types only (allows extra fields and accepts date as string or number).
- No Docker/Compose files or infrastructure were modified.
