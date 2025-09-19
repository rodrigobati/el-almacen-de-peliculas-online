# El Almacén de Películas – JPA/Hibernate

Proyecto adaptado para persistencia en **MySQL** usando **JPA/Hibernate** (sin Spring) y consultas inspiradas en el material:
- *TP Grupal – Almacén de Películas*
- *Spring Data – Consultas* (paginado, conteos, exists, búsquedas por campos, multi‑filtro)

## Estructura
- `src/main/java/unrn/infra/jpa`
  - `ActorEntity`, `DirectorEntity`, `PeliculaEntity` (+ `@NamedQueries`)
  - `PeliculaJpaRepository` (guardar, porId, búsquedas por título/género/actor/director, conteos, exists, paginado, rango de precio/fecha, Criteria)
  - `JPAUtil`, `PageResult`
- `src/main/java/unrn/app`
  - `DemoPersist` (persistencia básica)
  - `DemoConsultas` (consultas/paginado/criterios)
- `src/main/resources/META-INF/persistence.xml`

## Requisitos
- MySQL 8+
- Java 23 (configurado en `pom.xml`)
- Maven 3.9+

## Configuración
Crear DB y ajustar credenciales en `src/main/resources/META-INF/persistence.xml`:
```sql
CREATE DATABASE almacen DEFAULT CHARACTER SET utf8mb4;
```
Por defecto:
```
url=jdbc:mysql://localhost:3306/almacen?useSSL=false&serverTimezone=UTC
user=root
password=password
```

## Ejecutar
```bash
mvn -q -DskipTests package
mvn -q exec:java
```
El `exec-maven-plugin` apunta a `unrn.app.DemoConsultas`.
Podés cambiarlo a `DemoPersist` desde el `pom.xml` si querés probar inserción/lectura simple.

## Notas
- `hibernate.hbm2ddl.auto=update` solo para desarrollo.
- En producción, migrá a **Flyway** con scripts versionados (p. ej. `V1__init.sql`).

## Próximos pasos (opcional)
- Integrar **Spring Boot + Spring Data JPA** para repositorios `JpaRepository`, `Pageable`, `Sort`.
- Endpoints REST para búsquedas (título/género/actor/director), paginado y filtros.

## REST API
Se agregó una capa REST con **Spring Boot Web**:

- `GET /api/peliculas/{id}` → `DetallePeliculaDTO` (detalle para React)
- `GET /api/peliculas` con filtros opcionales `q, genero, formato, condicion, actor, director, minPrecio, maxPrecio, desde, hasta, page, size, sort, asc`

CORS habilitado para `/api/**`.

Ejecutar:
```bash
mvn -q -DskipTests spring-boot:run
# o
mvn -q -DskipTests package && java -jar target/el-almacen-de-peliculas-online-1.0-SNAPSHOT.jar
```
