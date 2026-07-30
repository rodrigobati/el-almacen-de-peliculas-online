# el-almacen-de-peliculas-online

Para levantar todo el proyecto hay que ubicarse en el directorio .\el-almacen-de-peliculas-online\ donde se encontrara el docker-compose.yml.

cd .\el-almacen-de-peliculas-online\
docker compose up

## Documentacion corta de vertical: Catalogo

### Proposito

La vertical Catalogo administra la oferta de peliculas del almacen online. Permite consultar peliculas y categorias desde la tienda, mantener peliculas/actores/directores desde backoffice, publicar cambios del catalogo y validar/reservar stock cuando Ventas solicita confirmar una compra.

El modulo Java esta en `el-almacen-de-peliculas-online/`.

### Servicios HTTP que expone

| Metodo | Endpoint interno | Proposito |
| --- | --- | --- |
| GET | `/peliculas` | Buscar peliculas con filtros, orden y paginacion. |
| GET | `/peliculas/{id}` | Consultar detalle publico de una pelicula. |
| GET | `/categorias` | Listar categorias/generos disponibles para filtros. |
| GET | `/api/admin/peliculas` | Listar peliculas para administracion. |
| POST | `/api/admin/peliculas` | Crear pelicula. |
| PUT | `/api/admin/peliculas/{id}` | Actualizar datos de pelicula. |
| PATCH | `/api/admin/peliculas/{id}/stock` | Actualizar stock con control de version. |
| DELETE | `/api/admin/peliculas/{id}` | Retirar/eliminar logicamente una pelicula. |
| GET/POST | `/api/admin/actores` | Buscar o crear actores. |
| GET/POST | `/api/admin/directores` | Buscar o crear directores. |

Via API Gateway se exponen principalmente como `/api/peliculas/**`, `/api/categorias/**` y `/api/admin/**`.

### Eventos que publica

| Exchange | Routing key / tipo | Cuando se publica |
| --- | --- | --- |
| `catalogo.events` | `MovieCreated.v1` | Alta de pelicula. |
| `catalogo.events` | `MovieUpdated.v1` | Actualizacion de pelicula. |
| `catalogo.events` | `MovieRetired.v1` | Retiro logico de pelicula. |
| `catalogo.events` | `catalogo.stock.validation.accepted` | Stock validado y reservado para una compra. |
| `catalogo.events` | `catalogo.stock.rechazado` | Stock rechazado por inexistencia, cantidad invalida o stock insuficiente. |

Los resultados de stock salen por outbox (`catalogo.outbox.scheduler.enabled=true`) para no perder eventos si falla RabbitMQ.

### Eventos que consume

| Exchange | Cola | Routing key / tipo | Proposito |
| --- | --- | --- | --- |
| `ventas.events` | `catalogo.stock.validation.requests` | `catalogo.stock.validation.requested` | Validar/reservar stock solicitado por Ventas. |
| `ventas.events` | `catalogo.q.ventas-compra-confirmada` | `ventas.compra.confirmada` | Flujo legacy de compra confirmada; deshabilitado por defecto. |
| `${rabbitmq.event.exchange.name}` | `rating.catalogo.queue` | `RatingActualizadoEvent.#` | Actualizar promedio y cantidad de ratings de una pelicula. |
| `${rabbitmq.event.exchange.name}` | `${rabbitmq.event.movie.queue.name}` | `Movie.#` | Listener legacy de eventos genericos de pelicula. |
