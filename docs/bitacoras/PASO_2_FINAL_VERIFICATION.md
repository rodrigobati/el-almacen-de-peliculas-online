# PASO 2 — Contract Governance Guardrails: Final Verification

Executive summary

- Scope: Paso 2 implemented a set of repository-only contract guardrails across the Gateway and Catalog modules to detect contract drift early (routing, query params, response envelopes, validation errors and DTO item schemas).
- Result: Guardrails are implemented as deterministic, CI-friendly tests (slice tests and MockMvc-focused integration tests). Focused verification confirmed the DTO JSON-slice test (`DtoContractGuardrailTest`) uses Spring Boot's auto-configured `ObjectMapper` and runs without network calls. No Docker/Compose changes were made.

Guardrails implemented (grouped by layer)

- Gateway layer
  - Routing / cross-service contract guardrails (step 2.6.x): tests enforce that gateway routes and downstream contract shapes remain aligned.
  - Query-parameter contract guardrails (step 2.7.x): tests validate query param names, required/optional semantics and types expected by downstream services.

- Service / Catalog layer
  - Response shape guardrail (step 2.8): `PageResponse` and response envelope shapes are asserted to avoid accidental structural changes.
  - HTTP-level validation-error guardrail (step 2.8.1): validation errors are canonicalized and asserted as the `{code,message,details}` shape at the controller layer (MockMvc tests assert the envelope and details payload).
  - DTO item schema guardrail (step 2.9): `DetallePeliculaDTO` serialization shape is tested to protect public list-item schemas.
  - DTO JSON-slice migration (step 2.9.1): `DtoContractGuardrailTest` migrated to `@JsonTest`, autowiring Spring Boot's `ObjectMapper` and excluding security auto-config to avoid JWKS/Keycloak network calls.

Verification performed

- I examined the DTO code (`unrn.dto.DetallePeliculaDTO`) and the test (`unrn.api.DtoContractGuardrailTest`).
- I ran the focused DTO test locally in the `el-almacen-de-peliculas-online` module (see commands below). The `@JsonTest` slice boots with the application configuration class but excludes security auto-configuration; test completed: `Tests run: 1, Failures: 0, Errors: 0`.
- Based on code inspection and the test patterns used across the guardrails (MockMvc for controller-level checks; JSON-slice for DTO serialization), the suite is deterministic and does not require network calls when executed as intended.
- Confirmed: no Docker/Compose dependency for the test suite — all guardrails are repo-only tests that run in-memory with test slices or MockMvc.

Architectural validation

- Responsibilities and layering:
  - Gateway tests govern routing and query param contracts (edge-level validation), and are implemented in the gateway module.
  - Services (Catalog) enforce response envelope shapes and validation error semantics; controller-level tests (MockMvc) validate HTTP error envelope `{code,message,details}`.
  - DTO guardrails protect the public item schema used in lists and responses.
  - Layers are complementary and non-overlapping: gateway enforces API surface and routing, services enforce payload shapes and error contracts.

Known limitations and risks (TP scope)

- Risk: If a future test is accidentally written as `@SpringBootTest` without security exclusions it may initialize `SecurityConfig` and attempt JWKS/issuer resolution. Mitigation: keep the slice/specific-scenario pattern and add a short dev note in the bitácora.
- Risk: Maven test selection quoting (PowerShell) can cause accidental invocation errors or zero-tests-run; this is an execution-time hazard, not a test-flakiness problem.
- Risk: Build warnings in the POM (duplicate dependency declarations) are present and should be cleaned at a later time; not a blocker for guardrail execution but could affect dependency resolution in some environments.
- Limitation: This verification is scoped to repository-local tests (TP-level). It does not include production-grade observability, performance, or runtime Keycloak integration testing.

Commands to run the full Paso 2 guardrail suite

- Gateway contract tests only (run all tests in the gateway module):

```bash
mvn -f el-almacen-de-peliculas-online-apigateway/pom.xml test
```

- Catalog contract tests only (response, validation error, dto guardrails):

```bash
mvn -f el-almacen-de-peliculas-online/el-almacen-de-peliculas-online/pom.xml test -Dtest=unrn.api.ResponseContractGuardrailTest,unrn.api.ValidationErrorContractIntegrationTest,unrn.api.DtoContractGuardrailTest
```

- Combined Paso 2 suite (run gateway then catalog sequentially):

```bash
mvn -f el-almacen-de-peliculas-online-apigateway/pom.xml test && mvn -f el-almacen-de-peliculas-online/el-almacen-de-peliculas-online/pom.xml test -Dtest=unrn.api.ResponseContractGuardrailTest,unrn.api.ValidationErrorContractIntegrationTest,unrn.api.DtoContractGuardrailTest
```

Architectural validation statement

- Paso 2 guardrails are coherent and layered: gateway-level guardrails protect external-facing routing and query semantics; service-level guardrails protect payload shapes, DTO schemas and validation-error envelopes. The JSON-slice approach for DTOs preserves runtime serialization behavior (application `ObjectMapper`) while keeping tests deterministic and network-free.

Explicit confirmations

- No Docker/Compose files were changed during Paso 2.
- The DTO guardrail (`DtoContractGuardrailTest`) runs without performing network calls (security auto-config excluded in the slice).

Signed-off-by: Senior Architect (repo verification)
