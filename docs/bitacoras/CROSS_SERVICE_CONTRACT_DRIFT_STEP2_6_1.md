# Cross-Service Contract Drift — Step 2.6.1 (Hardened)

Date: 2026-02-20

---

## Summary

Step 2.6.1 hardens the repo-only cross-service contract drift guardrail introduced in Step 2.6.
The goal is to replace fragile, ad-hoc parsing with robust, conventional mechanisms while keeping the
guardrail deterministic, fast, and network/Docker-free for CI.

Key changes:

- Gateway YAML parsing is implemented using SnakeYAML (test-scoped).
- Downstream controller discovery now uses classpath scanning and annotation introspection (reflection) instead of regex source scans.
- The guardrail remains bounded: only explicitly declared downstream modules and base packages are scanned.

## What changed vs Step 2.6

- Replaced hand-rolled YAML/text extraction with a real YAML parser (`org.yaml:snakeyaml`) to reliably read
  `spring.cloud.gateway.routes` entries and filter definitions.
- Replaced source-regex extraction of `@RequestMapping` with Spring's `ClassPathScanningCandidateComponentProvider`
  and direct annotation inspection of compiled controller classes. This removes fragility related to formatting
  and comments in source files.
- Added small unit tests around core path transformation helpers (`baseFromPredicate`, `stripPrefix`).

## Why this is more robust

- YAML parsing via SnakeYAML guarantees correct reading of lists, maps, and nested structures irrespective of
  formatting, indentation or use of quotes.
- Reflection-based discovery ensures we read the actual runtime annotations used by controllers (class-level
  `@RequestMapping`) and supports multiple mapped paths per controller.
- The test classpath includes downstream modules at test scope so discovery inspects the compiled classes,
  ensuring the guardrail checks the real artifacts that CI will execute.

## Inputs read by the guardrail

- `src/main/resources/application.yml` (gateway module)
- `src/main/resources/application-docker.yml` (gateway module)
- Compiled controller classes from downstream modules (test-scope dependency on those modules)
- Per-service `src/main/resources/application.yml` and `application-docker.yml` (to extract `server.servlet.context-path`)

## Compatibility rules (short)

1. For each gateway route entry read from YAML, extract the first `Path` predicate and any `StripPrefix` filter value.
2. Derive the base path by trimming wildcards (e.g., `/api/peliculas/**` → `/api/peliculas`).
3. Apply `StripPrefix=N` by removing the first N path segments, producing the forwarded prefix.
4. For the downstream service mapped to that route (explicit map in the test), discover class-level controller prefixes
   (from `@RequestMapping`) and prepend any `server.servlet.context-path` found in the service's resources.
5. The route is compatible if at least one effective controller prefix equals or starts with the forwarded prefix.

## Files added/modified

- Modified: `el-almacen-de-peliculas-online-apigateway/pom.xml` — added test-scope `snakeyaml` and test-scope dependencies
  on downstream modules so their compiled classes are available during tests.
- Added: `el-almacen-de-peliculas-online-apigateway/src/test/java/com/videoclub/apigateway/CrossServiceContractDriftHardenedTest.java`
  — hardened guardrail test.
- Added: `el-almacen-de-peliculas-online-apigateway/src/test/java/com/videoclub/apigateway/PathUtilsTest.java`
  — unit tests for core functions.

## How it loads YAML and mappings

- YAML is parsed via `new Yaml().load(InputStream)` producing nested `Map` and `List` structures.
- Routes are read from `spring.cloud.gateway.routes` and each route map is inspected for `id`, `predicates`, and `filters`.
- `StripPrefix` filter values are parsed from the `filters` list items (simple `key=value` parsing inside the test-scoped code).
- Controller discovery uses `ClassPathScanningCandidateComponentProvider` limited to explicit base packages supplied
  by the test contract map. Each candidate component's bean class is loaded via `Class.forName()` and inspected for
  `@RequestMapping` to obtain class-level base paths.

## How to run

Run from repository root (gateway module):

```bash
mvn -f el-almacen-de-peliculas-online-apigateway/pom.xml test -Dtest=CrossServiceContractDriftHardenedTest,PathUtilsTest
```

No Docker or external services required.

## Guarantees

- Detects mismatches where the gateway's forwarded prefix (after applying `StripPrefix`) no longer matches any
  downstream controller class-level base mapping (including compensation via `server.servlet.context-path`).
- Deterministic and fast; safe to run in CI as a unit/integration check.

## Remaining limitations

- Controllers that only declare method-level mappings (no class-level `@RequestMapping`) are not considered by the
  class-level mapping heuristic. This is a conscious trade-off to keep the check conservative and simple.
- Complex dynamic rewrites implemented by custom filters or programmatic logic are not fully validated.
- The check relies on test-scope availability of downstream modules; ensure CI runs the reactor build or provides
  the downstream artifacts on the test classpath.

## Remediation hints when guardrail fails

- If forwarding expects `/peliculas` but controllers expose `/api/peliculas`, either:
  - Remove the redundant `/api` prefix from controller class-level mappings, or
  - Configure `server.servlet.context-path` to include `/api` for the affected profile, or
  - Change the gateway route to apply/remove `StripPrefix` appropriately.

---

Prepared by: Senior Platform Engineer (Step 2.6.1 Hardened guardrail)
