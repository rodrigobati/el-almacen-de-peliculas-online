# Fix Report - Pelicula version invariant and optimistic locking

## 1) Domain patch (exact change)

File: src/main/java/unrn/model/Pelicula.java

```java
private void assertVersion(long version) {
    if (version < 0) {
        throw new RuntimeException(ERROR_VERSION_INVALIDA);
    }
}
```

## 2) Entity verification snippet (@Version)

File: src/main/java/unrn/infra/persistence/PeliculaEntity.java

```java
@Version
@Column(name = "version", nullable = false)
long version;
```

## 3) SQL migration script

File: MIGRATION-PELICULA-VERSION.sql

```sql
UPDATE pelicula
SET version = 0
WHERE version IS NULL OR version < 0;

ALTER TABLE pelicula
MODIFY COLUMN version BIGINT NOT NULL DEFAULT 0;

ALTER TABLE pelicula
ADD CONSTRAINT chk_pelicula_version_non_negative CHECK (version >= 0);
```

## 4) Workaround removal confirmation

Removed the version normalization (mappedVersion) in PeliculaEntity.asDomain so the
real persisted version is passed into the domain.

## 5) Retest results summary

- No token -> 401
- Admin token -> 200 with JSON array
- No HTTP 500

## 6) Final status

- Correct architectural fix applied

# Security Fix Report - Public catalog vs admin CRUD

## 1) Exact public routes found

Controller: unrn.api.CategoriaController

- @RequestMapping("/categorias")
- @GetMapping
- Method signature: public ResponseEntity<List<String>> listar()

Controller: unrn.api.PeliculaController

- @RequestMapping("/peliculas")
- @GetMapping
- Method signature: public ResponseEntity<PageResponse<DetallePeliculaDTO>> buscar(...)
- @GetMapping("/{id}")
- Method signature: public ResponseEntity<DetallePeliculaDTO> detalle(@PathVariable Long id)

## 2) Exact CRUD routes found

Controller: unrn.api.PeliculaAdminController

- @RequestMapping("/api/admin/peliculas")
- @PostMapping
- Method signature: public ResponseEntity<Void> crear(@RequestBody PeliculaRequest request)
- @PutMapping("/{id}")
- Method signature: public ResponseEntity<Void> actualizar(@PathVariable Long id, @RequestBody PeliculaRequest request)
- @DeleteMapping("/{id}")
- Method signature: public ResponseEntity<Void> eliminar(@PathVariable Long id)

## 3) Updated SecurityConfig snippet

File: src/main/java/unrn/config/SecurityConfig.java

```java
import org.springframework.http.HttpMethod;

// ...

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(csrf -> csrf.disable())
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/actuator/health").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/categorias").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/peliculas", "/peliculas/**").permitAll()
                                                .requestMatchers(HttpMethod.POST, "/api/admin/peliculas", "/api/admin/peliculas/**")
                                                .hasAuthority("ROLE_ADMIN")
                                                .requestMatchers(HttpMethod.PUT, "/api/admin/peliculas", "/api/admin/peliculas/**")
                                                .hasAuthority("ROLE_ADMIN")
                                                .requestMatchers(HttpMethod.PATCH, "/api/admin/peliculas", "/api/admin/peliculas/**")
                                                .hasAuthority("ROLE_ADMIN")
                                                .requestMatchers(HttpMethod.DELETE, "/api/admin/peliculas", "/api/admin/peliculas/**")
                                                .hasAuthority("ROLE_ADMIN")
                                                .anyRequest().authenticated())
                                .oauth2ResourceServer(oauth2 -> oauth2
                                                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
                return http.build();
        }
```

## 4) Curl verification commands

Public GET (no token):

```bash
curl -i http://localhost:8081/categorias
curl -i "http://localhost:8081/peliculas?page=0&size=12"
curl -i http://localhost:8081/peliculas/1
```

CRUD (no token):

```bash
curl -i -X POST http://localhost:8081/api/admin/peliculas -H "Content-Type: application/json" -d '{}'
curl -i -X PUT http://localhost:8081/api/admin/peliculas/1 -H "Content-Type: application/json" -d '{}'
curl -i -X DELETE http://localhost:8081/api/admin/peliculas/1
```

CRUD (admin token):

```bash
curl -i -X POST http://localhost:8081/api/admin/peliculas \
    -H "Authorization: Bearer <ADMIN_TOKEN>" \
    -H "Content-Type: application/json" \
    -d '{}'

curl -i -X PUT http://localhost:8081/api/admin/peliculas/1 \
    -H "Authorization: Bearer <ADMIN_TOKEN>" \
    -H "Content-Type: application/json" \
    -d '{}'

curl -i -X DELETE http://localhost:8081/api/admin/peliculas/1 \
    -H "Authorization: Bearer <ADMIN_TOKEN>"
```
