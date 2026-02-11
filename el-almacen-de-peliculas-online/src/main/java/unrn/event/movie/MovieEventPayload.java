package unrn.event.movie;

public record MovieEventPayload(
        Long movieId,
        String title,
        double price,
        boolean active,
        long version) {
}
