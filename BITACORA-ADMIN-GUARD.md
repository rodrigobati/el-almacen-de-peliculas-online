# Bitácora: Hardening de acceso a /admin

Fecha: 2026-02-19

## Resumen

Se aplicaron cambios focalizados en el frontend para evitar que usuarios no administradores (`patito`) puedan acceder o renderizar rutas bajo `/admin/*`, y para asegurar que el listado del catálogo en la UI admin obtiene los datos desde el endpoint protegido `/api/admin/peliculas` usando `Authorization`.

No se modificó nada en Docker o en la infraestructura. Sólo archivos del frontend fueron editados.

## Problema original

- Un usuario no-admin (`patito`) podía abrir `http://localhost:5173/admin/catalogo` y ver la UI de administración con la lista de películas.
- Los intentos de acciones administrativas (retirar/editar/crear) devolvían HTTP 403, lo que probaba que el backend estaba protegiendo las acciones, pero el listado provenía de un endpoint público.

## Evidencia antes del cambio

- `src/pages/AdminCatalogo.jsx` llamaba a `listMovies()` para cargar la tabla.
- `src/api/catalogoAdmin.js` implementaba `listMovies()` apuntando a `${API_BASE}/peliculas?...` y hacía `fetch(url)` sin header `Authorization`.
- Admin actions (create/update/delete) usaban endpoints `/api/admin/...` con `Authorization` y recibían 403 para `patito`.

## Cambios aplicados (por archivo)

- `src/contexts/AuthContext.jsx`
  - Añadida constante `ADMIN_ROLE = "ROLE_ADMIN"`.
  - Expuesto `hasRole(roleName)` y `isAdmin` en el contexto (`useAuth()` ahora devuelve `isAdmin` y `hasRole`).
  - `roles` continúa siendo calculado a partir de `tokenParsed.realm_access.roles` y `tokenParsed.resource_access` (mismo comportamiento anterior).

- `src/components/AccessDenied.jsx`
  - Nuevo componente simple que muestra un mensaje `Acceso denegado (403)`.

- `src/components/AdminRoute.jsx`
  - Nuevo componente de ruta. Reglas:
    - Si `loading`: se muestra spinner de carga.
    - Si no autenticado: `Navigate` a `/` (misma experiencia que `ProtectedRoute`).
    - Si autenticado pero no admin: renderiza `AccessDenied`.
    - Si admin: renderiza `children`.

- `src/App.jsx`
  - La navegación superior ahora muestra el enlace a `/admin/catalogo` sólo si `isAuthenticated && isAdmin`.
  - La ruta `/admin/catalogo` ahora está envuelta en `<AdminRoute>` en lugar de `ProtectedRoute`.

- `src/api/catalogoAdmin.js`
  - `listMovies` ahora requiere `accessToken` como primer parámetro.
  - `listMovies(accessToken, params)` invoca `${API_BASE}/admin/peliculas?...` y envía `Authorization: Bearer <token>`.
  - Si `accessToken` ausente lanza error estructurado `AUTH_TOKEN_MISSING` (manteniendo la convención del módulo).

- `src/pages/AdminCatalogo.jsx`
  - Todas las llamadas a `listMovies(...)` ahora pasan `accessToken` obtenido desde `useAuth()` (`keycloak?.token`) — por ejemplo `listMovies(accessToken, { q, page, size })`.

## Por qué estos cambios

- Evitar exposición de datos sensibles en UI (no-admins no deben ver páginas administrativas ni listados diseñados para administración).
- Mantener backend como autoridad: se sigue esperando que los endpoints `/api/admin/**` estén protegidos por roles en server-side.

## Verificación (pasos)

1. Como `patito` (no-admin)
   - Abrir `http://localhost:5173/admin/catalogo`.
   - Resultado esperado: no se debe renderizar la UI admin. Se mostrará la pantalla `Acceso denegado (403)` o serás redirigido a `/` si no estás autenticado.
   - Confirmar en DevTools → Network que NO se efectúa `GET /api/admin/peliculas` y no se solicita lista.

2. Como admin
   - Iniciar sesión con cuenta admin.
   - Abrir `http://localhost:5173/admin/catalogo`.
   - Resultado esperado: la UI admin se renderiza y realiza `GET /api/admin/peliculas?...` con header `Authorization: Bearer <token>`; respuesta 200 muestra la lista.

3. Acciones administrativas
   - Como admin: `Retirar` / `Editar` / `Crear` funcionan (backend responde 200/201 según corresponda).
   - Como `patito`: acciones administrativas siguen devolviendo 403.

## Notas sobre rebuild/restart

- Sólo se cambiaron archivos del frontend (React/Vite). No se hicieron cambios en contenedores ni en Docker Compose.
- Si estás ejecutando el frontend vía Vite dev server, reiniciá el dev server para que tome los cambios (no es necesario rebuild de imágenes ni reiniciar contenedores):

```bash
# desde el front-end folder
npm run dev
```

o si usás la integración con Docker dev container, reiniciá únicamente el proceso de front-end (no tocar Docker Compose).

## Próximos pasos sugeridos

1. Revisar y confirmar la configuración de roles en Keycloak: comprobar que el rol usado sea `ROLE_ADMIN` (o cambiar `ADMIN_ROLE` en `AuthContext.jsx` para ajustar al rol real).
2. Revisar la protección server-side para `/api/admin/**` (Spring Security / API Gateway). La UI ya bloquea y usa endpoint protegido, pero el backend debe garantizar seguridad.

## Archivos cambiados (resumen)

- `src/contexts/AuthContext.jsx` — expone `hasRole` e `isAdmin`.
- `src/components/AccessDenied.jsx` — nuevo.
- `src/components/AdminRoute.jsx` — nuevo.
- `src/App.jsx` — oculta link admin y usa `AdminRoute`.
- `src/api/catalogoAdmin.js` — `listMovies` ahora usa `/admin/peliculas` y requiere token.
- `src/pages/AdminCatalogo.jsx` — pasa `accessToken` a `listMovies`.

No se realizaron cambios en el backend ni en la infraestructura.
