# Catalogo

## Proposito

Catalogo administra peliculas, categorias, actores, directores y stock visible del sistema. Expone consultas publicas para la tienda, endpoints administrativos para backoffice y eventos de integracion para mantener sincronizadas las verticales de Ventas y Rating.

## Servicios HTTP que expone

| Metodo | Endpoint interno | Proposito |
| --- | --- | --- |
| GET | `/peliculas` | Buscar peliculas con filtros, orden y paginacion. |
| GET | `/peliculas/{id}` | Consultar detalle publico de una pelicula. |
| GET | `/categorias` | Listar categorias/generos. |
| GET | `/api/admin/peliculas` | Listar peliculas para administracion. |
| POST | `/api/admin/peliculas` | Crear pelicula. |
| PUT | `/api/admin/peliculas/{id}` | Actualizar pelicula. |
| PATCH | `/api/admin/peliculas/{id}/stock` | Actualizar stock con control de version. |
| DELETE | `/api/admin/peliculas/{id}` | Retirar/eliminar logicamente una pelicula. |
| GET/POST | `/api/admin/actores` | Buscar o crear actores. |
| GET/POST | `/api/admin/directores` | Buscar o crear directores. |

Via API Gateway se consumen como `/api/peliculas/**`, `/api/categorias/**` y `/api/admin/**`.

## Eventos que publica

| Exchange | Routing key / tipo | Proposito |
| --- | --- | --- |
| `catalogo.events` | `MovieCreated.v1` | Notificar alta de pelicula. |
| `catalogo.events` | `MovieUpdated.v1` | Notificar actualizacion de pelicula. |
| `catalogo.events` | `MovieRetired.v1` | Notificar retiro logico de pelicula. |
| `catalogo.events` | `catalogo.stock.validation.accepted` | Confirmar a Ventas que el stock fue validado y reservado. |
| `catalogo.events` | `catalogo.stock.rechazado` | Informar a Ventas que no se pudo reservar stock. |

## Eventos que consume

| Exchange | Cola | Routing key / tipo | Proposito |
| --- | --- | --- | --- |
| `ventas.events` | `catalogo.stock.validation.requests` | `catalogo.stock.validation.requested` | Validar stock antes de confirmar una compra. |
| `ventas.events` | `catalogo.q.ventas-compra-confirmada` | `ventas.compra.confirmada` | Flujo legacy, deshabilitado por defecto. |
| `${rabbitmq.event.exchange.name}` | `rating.catalogo.queue` | `RatingActualizadoEvent.#` | Actualizar rating promedio en peliculas. |
| `${rabbitmq.event.exchange.name}` | `${rabbitmq.event.movie.queue.name}` | `Movie.#` | Integracion legacy de eventos genericos de pelicula. |
