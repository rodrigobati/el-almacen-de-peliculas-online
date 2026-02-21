# PROYECCION_VERIFICACION_2026-02-18_22-36

## Fecha y hora de ejecución

2026-02-18 22:36 (hora local)

## 1. MovieId enviado desde frontend

Payload capturado en evidencia de runtime:

```http
POST http://localhost:9500/api/carrito/items
Body: {"peliculaId":"1","titulo":"Matrix","precioUnitario":100,"cantidad":1}
```

Observación:

- El frontend actual envía el ID en el campo `peliculaId` (valor: `"1"`), no en `movieId`.
- Para esta verificación se usó el ID efectivo enviado: `1`.

## 2. Verificación en almacen_ventas.pelicula_proyeccion

Contenedor MySQL validado para `ventas-service`: `catalogo-mysql`.

### 2.1 Conteo total de filas

SQL:

```sql
USE almacen_ventas;
SELECT COUNT(*) AS total_filas FROM pelicula_proyeccion;
```

Resultado:

```text
total_filas
1
```

### 2.2 Búsqueda por movie_id específico

SQL:

```sql
USE almacen_ventas;
SELECT movie_id, titulo, precio_actual, activa, version
FROM pelicula_proyeccion
WHERE movie_id = '1';
```

Resultado:

```text
movie_id | titulo        | precio_actual | activa | version
1        | Blade Runner  | 9999.99       | 1      | 1
```

### 2.3 Validación de campos críticos

- activa
- precio_actual
- version

SQL:

```sql
USE almacen_ventas;
SELECT movie_id,
       (activa = 1) AS activa_es_1,
       (precio_actual IS NOT NULL) AS precio_no_nulo,
       version
FROM pelicula_proyeccion
WHERE movie_id = '1';
```

Resultado:

```text
movie_id | activa_es_1 | precio_no_nulo | version
1        | 1           | 1              | 1
```

## 3. Comparación contra almacen_peliculas.pelicula (catálogo)

SQL:

```sql
USE almacen_peliculas;
SELECT id, titulo, precio, activa, version
FROM pelicula
WHERE id = 1;
```

Resultado:

```text
id | titulo        | precio  | activa | version
1  | Blade Runner  | 9999.99 | 1      | 0
```

SQL de comparación cruzada:

```sql
SELECT v.movie_id,
       v.titulo AS titulo_proy,
       v.precio_actual AS precio_proy,
       v.version AS version_proy,
       c.id AS id_catalogo,
       c.titulo AS titulo_cat,
       c.precio AS precio_cat,
       c.version AS version_cat,
       c.activa AS activa_cat
FROM almacen_ventas.pelicula_proyeccion v
LEFT JOIN almacen_peliculas.pelicula c
  ON c.id = CAST(v.movie_id AS UNSIGNED)
WHERE v.movie_id = '1';
```

Resultado:

```text
movie_id | titulo_proy   | precio_proy | version_proy | id_catalogo | titulo_cat    | precio_cat | version_cat | activa_cat
1        | Blade Runner  | 9999.99     | 1            | 1           | Blade Runner  | 9999.99    | 0           | 1
```

## 4. Análisis

- ¿Existe la fila?
  - Sí, para `movie_id='1'` existe una fila en `pelicula_proyeccion`.
- ¿Está activa?
  - Sí, `activa=1`.
- ¿Hay desalineación de ID?
  - Hay diferencia de tipo entre tablas (`pelicula_proyeccion.movie_id` es `VARCHAR(64)` y `pelicula.id` es `BIGINT`), pero para el valor `1` no genera mismatch funcional (`match_string=1`, `match_numeric_cast=1`).
- ¿Hay desincronización evento → proyección?
  - Parcialmente sí: catálogo tiene más películas (`total_catalogo=23`) y `pelicula_proyeccion` tiene solo `1` fila en esta ejecución. Esto sugiere proyección incompleta/desactualizada para otros IDs.

## 5. Conclusión técnica

Escenario que aplica en esta ejecución:

- **Proyección desactualizada (parcial)**.

Detalle final:

- Para el ID verificado (`1`), **los datos están correctos** en proyección (existe, activa y con precio no nulo).
- Si el error “La película no existe en la proyección” persiste para otras películas, la causa probable es que esos IDs no estén aún materializados en `almacen_ventas.pelicula_proyeccion`.
- Si el payload real de DevTools en el momento del error no fue `peliculaId="1"`, ese ID puntual debe validarse con las mismas consultas para confirmación definitiva.

No Docker changes were made.
