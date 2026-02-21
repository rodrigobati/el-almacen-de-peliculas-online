# Diagnóstico Add to Cart - El Almacén de Películas Online

## 1. Summary

El flujo de “Agregar al carrito” falla en backend por un error SQL real: la consulta a `pelicula_proyeccion` se ejecuta desde `ventas`, pero la tabla no existe en la base `almacen_ventas`. El `POST /api/carrito/items` llega correctamente al servicio de ventas y termina en `BadSqlGrammarException`/`SQLSyntaxErrorException`, que luego se mapea a `400` con código `COMPRA_ERROR`.

## 2. Evidence collected

- Frontend construye `POST http://localhost:9500/api/carrito/items` en `src/api/carrito.js`.
- Gateway enruta `/api/carrito/**` a `ventas-service` con `StripPrefix=1`.
- Controller de ventas recibe en `POST /carrito/items`.
- Servicio invoca `proyeccionRepository.buscarPorMovieId(...)`.
- Query exacta:
  - `SELECT movie_id, titulo, precio_actual, activa, version FROM pelicula_proyeccion WHERE movie_id = ?`
- Reproducción autenticada:
  - `POST /api/carrito/items` -> `400`
  - body: `{"code":"COMPRA_ERROR","message":"PreparedStatementCallback; bad SQL grammar [...]"}`
- Logs de `ventas-service`:
  - `Caused by: java.sql.SQLSyntaxErrorException: Table 'almacen_ventas.pelicula_proyeccion' doesn't exist`
- Inspección DB en runtime (`catalogo-mysql`, DB `almacen_ventas`):
  - Existen: `carrito`, `carrito_item`, `compra`, `compra_item`, `outbox_event`, `processed_events`
  - No existe: `pelicula_proyeccion`
- `schema.sql` de ventas sí define `CREATE TABLE IF NOT EXISTS pelicula_proyeccion (...)`.

## 3. Hypotheses (ranked)

1. **Tabla `pelicula_proyeccion` no creada en `almacen_ventas`** (más probable)
   - Encaja exactamente con la causa de la excepción SQL en logs.
   - Verificación rápida: `SHOW TABLES LIKE 'pelicula_proyeccion';` en `almacen_ventas`.

2. **`schema.sql` no se ejecuta en perfil docker de ventas**
   - En tests está `spring.sql.init.mode=always`, en docker no.
   - Verificación rápida: comparar `src/test/resources/application.properties` vs `src/main/resources/application-docker.properties`.

3. **Desalineación entre tablas creadas por JPA y tabla de proyección JDBC**
   - JPA crea entidades persistentes (`carrito`, `compra`, etc.), pero `pelicula_proyeccion` depende de inicialización SQL explícita.

4. **Mismatch de columnas/tablas en query** (menos probable)
   - Si la tabla existiera, podría fallar por columna; aquí falla por tabla inexistente.

## 4. Root cause (most likely)

La causa raíz más probable es que en runtime Docker del servicio `ventas` **no se está creando la tabla `pelicula_proyeccion`** en la base `almacen_ventas`; la query JDBC del repository depende de ella y falla con `BadSqlGrammarException`.

## 5. Proposed fix (no code yet)

1. Habilitar inicialización SQL del esquema en perfil docker de ventas.
   - Archivo objetivo: `el-almacen-de-peliculas-online-ventas/src/main/resources/application-docker.properties`
   - Cambio mínimo propuesto:
     - `spring.sql.init.mode=always`
     - opcional explícito: `spring.sql.init.schema-locations=classpath:schema.sql`
2. Mantener query actual del repository si el esquema queda alineado.
3. Verificar que la proyección se alimente con eventos (`MovieEventHandler`) para que existan filas.

## 6. Tests to add/update

1. `agregarPelicula_proyeccionDisponible_retornaCarritoActualizado`
   - Verifica `POST /api/carrito/items` exitoso con fila en `pelicula_proyeccion`.

2. `agregarPelicula_proyeccionInexistente_retornaErrorEsperado`
   - Verifica fallo controlado cuando no hay proyección.

3. `inicioAplicacion_conPerfilDocker_creaTablaPeliculaProyeccion`
   - Verifica metadata SQL al arranque.

4. `buscarPorMovieId_tablaConDatos_devuelveProyeccion`
   - Verifica mapping JDBC de columnas (`movie_id`, `titulo`, `precio_actual`, `activa`, `version`).

## 7. Verification plan

1. Frontend
   - Acción: click en “Agregar al carrito” desde UI.
   - Esperado: toast de éxito.

2. HTTP por gateway
   - `POST http://localhost:9500/api/carrito/items` con token válido.
   - Esperado: `200` y carrito actualizado.

3. DB queries
   - `SHOW TABLES LIKE 'pelicula_proyeccion';`
   - `DESCRIBE pelicula_proyeccion;`
   - `SELECT * FROM pelicula_proyeccion LIMIT 10;`

4. Logs
   - Verificar ausencia de `BadSqlGrammarException` / `Table ... doesn't exist`.

No Docker changes were made.
