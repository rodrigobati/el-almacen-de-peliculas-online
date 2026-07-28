# DTO Contract Guardrail — Step 2.9

This document records the guardrail for DTO schema validation (Step 2.9) and the migration to a JSON slice test in Step 2.9.1.

## Step 2.9.1 — Migration to @JsonTest

Reason for migration

- Avoid starting a full Spring application context during DTO serialization tests which previously triggered web/security auto-configuration and remote JWKS/Keycloak lookups.
- Keep tests deterministic and CI-friendly while still using the application's Jackson configuration.

What changed

- The `DtoContractGuardrailTest` was migrated from a full-context test to a JSON slice test using `@JsonTest`.
- The test now autowires Spring Boot's auto-configured `ObjectMapper` (so the serialization matches runtime behavior), rather than constructing a local mapper instance.
- Security and OAuth2 resource-server auto-configurations are excluded via `spring.autoconfigure.exclude` to ensure no security beans or network calls are initialized during the test.

How the test behaves

- The test constructs a minimal `DetallePeliculaDTO` (using the real DTO constructor), serializes it with the autowired `ObjectMapper` and asserts the presence and JSON node types of mandatory fields only.
- Assertions are conservative and backward-compatible:
  - Only mandatory fields are asserted for presence and type (`id`, `titulo`, `precio`, `directores`, `actores`, `fechaSalida`, `rating`).
  - `fechaSalida` is accepted as either textual (ISO date) or numeric (timestamp).
  - Optional fields (`ratingPromedio`, `totalRatings`) are validated only if present (must be numeric or null).
  - Extra fields are allowed (test does not fail on additional properties).

How to run the focused DTO guardrail test

Run only the DTO guardrail test (from repository root):

```bash
mvn -f el-almacen-de-peliculas-online/el-almacen-de-peliculas-online/pom.xml test -Dtest=unrn.api.DtoContractGuardrailTest
```

Run the contract guardrail suite (response + validation + dto):

```bash
mvn -f el-almacen-de-peliculas-online/el-almacen-de-peliculas-online/pom.xml test -Dtest=unrn.api.ResponseContractGuardrailTest,unrn.api.ValidationErrorContractIntegrationTest,unrn.api.DtoContractGuardrailTest
```

Notes

- No Docker/Compose files were modified as part of this change.
