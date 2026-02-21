# FIX_REPORT

## Summary

El fallo de **Agregar al carrito** se debía a que `ventas-service` ejecutaba una query JDBC contra `pelicula_proyeccion`, pero esa tabla no existía en runtime Docker en `almacen_ventas`. La tabla sí está declarada en `schema.sql`, pero no se inicializaba explícitamente en el perfil Docker.

## Files changed

- `el-almacen-de-peliculas-online-ventas/src/main/resources/application-docker.properties`
- `el-almacen-de-peliculas-online-ventas/src/test/java/unrn/repository/PeliculaProyeccionRepositoryIntegrationTest.java`

## Exact properties added/changed

En `application-docker.properties` se agregaron:

- `spring.sql.init.mode=always`
- `spring.sql.init.schema-locations=classpath:schema.sql`

Con esto, `schema.sql` se ejecuta explícitamente en perfil Docker y crea `pelicula_proyeccion` antes del uso del repository JDBC.

## Tests added

Clase nueva:

- `unrn.repository.PeliculaProyeccionRepositoryIntegrationTest`

Métodos:

- `inicioAplicacion_tablaPeliculaProyeccionExiste_true`
- `buscarPorMovieId_tablaConDatos_devuelveProyeccion`

Resultado de ejecución focalizada:

- `mvn -Dtest=PeliculaProyeccionRepositoryIntegrationTest test` → **BUILD SUCCESS**

## Verification steps

1. Rebuild/restart (si aplica):

```bash
docker compose -f docker-compose-full.yml up -d --build --force-recreate ventas-service api-gateway
```

2. DB checks:

```bash
docker exec catalogo-mysql mysql -uroot -proot -D almacen_ventas -e "SHOW TABLES LIKE 'pelicula_proyeccion';"
docker exec catalogo-mysql mysql -uroot -proot -D almacen_ventas -e "DESCRIBE pelicula_proyeccion;"
```

3. HTTP check (autenticado):

```bash
POST http://localhost:9500/api/carrito/items
Body: {"peliculaId":"1","titulo":"Matrix","precioUnitario":100,"cantidad":1}
```

Esperado: respuesta 2xx con carrito actualizado.

4. Log checks:

```bash
docker logs ventas-service --tail 300
```

Esperado: sin `Table 'almacen_ventas.pelicula_proyeccion' doesn't exist` y sin `BadSqlGrammarException` para ese endpoint.

## Failure fallback (if still failing)

Si el error persiste, el motivo más probable es que `ventas-service` no esté levantando el perfil Docker esperado o esté usando un datasource distinto al configurado. Ajuste mínimo siguiente:

- confirmar `SPRING_PROFILES_ACTIVE=docker` en runtime,
- confirmar URL efectiva de datasource en logs,
- recrear solo `ventas-service` para forzar init de esquema.

No Docker changes were made.
