package unrn.event.movie;

import java.time.Instant;
import java.util.UUID;

/**
 * Envoltorio de evento saliente para cambios de peliculas del catalogo.
 *
 * Agrega eventId, tipo versionado, fecha de ocurrencia y payload para publicar
 * altas, actualizaciones y retiros de peliculas de forma consistente hacia otras
 * verticales.
 */
public record MovieEventEnvelope(
        String eventId,
        String eventType,
        Instant occurredAt,
        MovieEventPayload payload) {

    static final String TYPE_CREATED = "MovieCreated.v1";
    static final String TYPE_UPDATED = "MovieUpdated.v1";
    static final String TYPE_RETIRED = "MovieRetired.v1";

    /**
     * Crea el envelope versionado para informar el alta de una pelicula.
     */
    public static MovieEventEnvelope created(MovieEventPayload payload) {
        return new MovieEventEnvelope(UUID.randomUUID().toString(), TYPE_CREATED, Instant.now(), payload);
    }

    /**
     * Crea el envelope versionado para informar cambios en una pelicula existente.
     */
    public static MovieEventEnvelope updated(MovieEventPayload payload) {
        return new MovieEventEnvelope(UUID.randomUUID().toString(), TYPE_UPDATED, Instant.now(), payload);
    }

    /**
     * Crea el envelope versionado para informar el retiro logico de una pelicula.
     */
    public static MovieEventEnvelope retired(MovieEventPayload payload) {
        return new MovieEventEnvelope(UUID.randomUUID().toString(), TYPE_RETIRED, Instant.now(), payload);
    }
}
