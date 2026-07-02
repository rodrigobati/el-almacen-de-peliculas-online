package unrn.event.movie;

/**
 * Payload minimo que describe una pelicula en eventos de catalogo.
 *
 * Incluye solo los datos que otros servicios necesitan para reaccionar a cambios:
 * identificador, titulo, precio, estado activo y version del agregado.
 */
public record MovieEventPayload(
        Long movieId,
        String title,
        double price,
        boolean active,
        long version) {
}
