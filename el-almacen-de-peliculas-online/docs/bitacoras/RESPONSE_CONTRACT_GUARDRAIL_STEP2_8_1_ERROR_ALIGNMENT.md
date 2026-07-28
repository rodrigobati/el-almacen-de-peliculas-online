```markdown
# RESPONSE CONTRACT GUARDRAIL — STEP 2.8.1 ERROR ALIGNMENT

Date: 2026-02-20

## Plan

- Add HTTP-level MockMvc integration tests that assert validation errors return the canonical `{ code, message, details }` shape.
- Keep existing serialization guardrail for `PageResponse` and `ApiError`.
- Verify `ApiErrorHandler` has a dedicated handler for `CatalogoQueryValidationException` and that it is chosen over the generic `Exception` handler.
- Add a bitácora documenting changes and how to run the focused tests.

## Files added

- `src/test/java/unrn/api/ValidationErrorContractIntegrationTest.java` — new integration tests that trigger validation errors via `GET /peliculas`.
- `docs/bitacoras/RESPONSE_CONTRACT_GUARDRAIL_STEP2_8_1_ERROR_ALIGNMENT.md` — this document.

## Code changes (exact additions)

1) New test: `ValidationErrorContractIntegrationTest.java` (excerpt):

```java
@SpringBootTest(classes = Application.class, properties = {
        "spring.rabbitmq.listener.simple.auto-startup=false",
        "spring.rabbitmq.listener.direct.auto-startup=false"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ValidationErrorContractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void beforeEach() { /* truncate tables same as other catalog tests */ }

    @Test
    void listarPublico_sizeCero_devuelveValidationError() throws Exception {
        mockMvc.perform(get("/peliculas").param("size","0").param("page","0").param("sort","titulo")).andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("INVALID_SIZE"))
            .andExpect(jsonPath("$.message").isString())
            .andExpect(jsonPath("$.details.field").value("size"))
            .andExpect(jsonPath("$.details.rule").isString());
    }
}
```

No runtime code modifications to handlers were required because `ApiErrorHandler` already exposes a specific `@ExceptionHandler(CatalogoQueryValidationException.class)` that returns a `ValidationError` record.

## Commands to verify (run focused tests)

Run only the new validation contract tests:

```bash
mvn -f el-almacen-de-peliculas-online/el-almacen-de-peliculas-online/pom.xml test -Dtest=unrn.api.ValidationErrorContractIntegrationTest
```

Run the serialization guardrail and the new validation tests together:

```bash
mvn -f el-almacen-de-peliculas-online/el-almacen-de-peliculas-online/pom.xml test -Dtest=unrn.api.ResponseContractGuardrailTest,unrn.api.ValidationErrorContractIntegrationTest
```

## Bitácora completa

Problem statement:

- Step 2.8 added serialization tests for `PageResponse` and `ApiError` but did not assert the canonical validation-error contract `{code,message,details}` declared in Step 2.2. This allowed a risk where validation errors could regress to the generic `ApiError` shape without tests failing.

Decision/Policy:

- Validation errors caused by `CatalogoQueryValidationException` are canonical and must return `{ code, message, details }` (HTTP 400).
- Generic/unhandled errors may return `ApiError { message, status, path, timestamp }`.

Files changed/added:

- `src/test/java/unrn/api/ValidationErrorContractIntegrationTest.java` — integration tests asserting validation error responses for paging/sort params.
- `docs/bitacoras/RESPONSE_CONTRACT_GUARDRAIL_STEP2_8_1_ERROR_ALIGNMENT.md` — this report.

Tests added (names and key assertions):

- `listarPublico_pageNegativo_devuelveValidationError` — triggers `page=-1`, asserts 400, `code=INVALID_PAGE`, `details.field=page`.
- `listarPublico_sizeCero_devuelveValidationError` — triggers `size=0`, asserts 400, `code=INVALID_SIZE`, `details.field=size`, `details.rule` present.
- `listarPublico_sizeExcesivo_devuelveValidationError` — triggers `size=101`, asserts 400, `code=INVALID_SIZE`, `details.rule=="1..100"`.
- `listarPublico_sortInvalido_devuelveValidationError` — triggers invalid `sort`, asserts 400, `code=INVALID_SORT`, `details.field=sort`.

How to run focused tests:

```bash
mvn -f el-almacen-de-peliculas-online/el-almacen-de-peliculas-online/pom.xml test -Dtest=unrn.api.ValidationErrorContractIntegrationTest
```

Confirmation: no Docker/Compose files were modified.

## Acceptance criteria

- For each invalid parameter request the API returns HTTP 400 and body contains `code` (string), `message` (string), and `details` (object).
- `code` equals the canonical codes: `INVALID_PAGE`, `INVALID_SIZE`, `INVALID_SORT` as applicable.
- `details.field` identifies the parameter that failed and `details.rule` is present when applicable.
- `ResponseContractGuardrailTest` continues to validate `PageResponse` top-level fields and `ApiError` serialization.
- All new tests are repo-only (MockMvc + SpringBootTest), deterministic, and require no external services.

---

Prepared by: Senior Platform Engineer (Contract Governance)

```
