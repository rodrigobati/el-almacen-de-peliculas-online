# Reporte de Fix - Flujo de Retiro de Películas

## 1) Resumen ejecutivo

Se corrigió el comportamiento de retiro de películas en catálogo.

Situación detectada:

- El endpoint `DELETE /api/admin/peliculas/{id}` devolvía `204`.
- La persistencia guardaba correctamente `activa = false`.
- Pero la película seguía apareciendo en los listados admin y públicos.

Resultado final:

- Las películas retiradas quedan excluidas de los listados.
- Se agregaron tests de regresión.
- Se validó fin a fin con evidencia de red + base de datos.

---

## 2) Síntoma reproducido

Reproducción previa al fix:

- `DELETE_STATUS=204`
- `ADMIN_LIST_AFTER_COUNT=23`
- `ID_PRESENT_AFTER_DELETE=True`

Conclusión:

- El retiro se ejecutaba, pero el listado no respetaba el estado lógico de baja.

---

## 3) Verificación de persistencia

Consulta en MySQL (antes y después del fix) confirmó baja lógica:

```sql
SELECT id, titulo, activa, version
FROM pelicula
WHERE id = <ID_RETIRADA>;
```

Evidencia observada:

- `activa = 0` (false)
- `version` incrementada según flujo

Esto confirmó que el problema no era de persistencia, sino de lectura/listado.

---

## 4) Causa raíz única

Los métodos de lectura en repositorio no filtraban por `activa=true`:

- `listarTodos()` devolvía todas las películas.
- `buscarPaginado(...)` tampoco incluía predicado por estado activo.

Por eso, aunque `eliminar(id)` guardaba `activa=false`, la película seguía visible.

---

## 5) Fix mínimo aplicado

Archivo modificado:

- `el-almacen-de-peliculas-online/src/main/java/unrn/infra/persistence/PeliculaRepository.java`

Cambios:

1. Se agregó predicado global de activas en `buildPredicates(...)`:
   - `preds.add(cb.isTrue(root.get("activa")));`
2. Se actualizó `listarTodos()` para devolver solo activas:
   - `SELECT p FROM PeliculaEntity p WHERE p.activa = true ORDER BY p.titulo`

No se modificó el contrato de API, solo el criterio de lectura.

---

## 6) Tests de regresión agregados

Archivo modificado:

- `el-almacen-de-peliculas-online/src/test/java/unrn/infra/persistence/PeliculaRepositoryIntegrationTest.java`

Nuevos tests:

1. `listarTodos_excluyePeliculasRetiradas`
2. `buscarPaginado_excluyePeliculasRetiradas`

Además, se alineó la configuración del test para evitar dependencia externa del issuer JWT durante carga de contexto:

- `spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost/jwks`
- listeners de Rabbit desactivados en test

Resultado de ejecución:

- `passed=4`
- `failed=0`

---

## 7) Validación fin a fin posterior al fix

Evidencia API (post-fix):

- `DELETE_STATUS=204`
- `ADMIN_BEFORE_COUNT=18`
- `ADMIN_AFTER_COUNT=17`
- `ADMIN_AFTER_HAS_ID=False`
- `PUBLIC_AFTER_HAS_ID=False`

Evidencia DB (post-fix):

- `id=31, activa=0`

Conclusión:

- El retiro se persiste y ahora también se refleja correctamente en las lecturas admin y públicas.

---

## 8) Impacto

Impacto funcional:

- Se corrige inconsistencia entre estado persistido y estado visible en UI/listados.

Riesgo:

- Bajo. El cambio se limita a filtros de lectura de películas activas y está cubierto con tests.

---

## 9) Archivos tocados

- `el-almacen-de-peliculas-online/src/main/java/unrn/infra/persistence/PeliculaRepository.java`
- `el-almacen-de-peliculas-online/src/test/java/unrn/infra/persistence/PeliculaRepositoryIntegrationTest.java`
