package unrn.event.movie;

import java.time.Instant;
import java.util.UUID;

public record MovieEventEnvelope(
        String eventId,
        String eventType,
        Instant occurredAt,
        MovieEventPayload payload) {

    static final String TYPE_CREATED = "MovieCreated.v1";
    static final String TYPE_UPDATED = "MovieUpdated.v1";
    static final String TYPE_RETIRED = "MovieRetired.v1";

    public static MovieEventEnvelope created(MovieEventPayload payload) {
        return new MovieEventEnvelope(UUID.randomUUID().toString(), TYPE_CREATED, Instant.now(), payload);
    }

    public static MovieEventEnvelope updated(MovieEventPayload payload) {
        return new MovieEventEnvelope(UUID.randomUUID().toString(), TYPE_UPDATED, Instant.now(), payload);
    }

    public static MovieEventEnvelope retired(MovieEventPayload payload) {
        return new MovieEventEnvelope(UUID.randomUUID().toString(), TYPE_RETIRED, Instant.now(), payload);
    }
}
