package unrn.event;

/**
 * DTO para recibir eventos de actualización de rating desde la vertical Rating.
 * Representa los datos agregados de rating de una película.
 */
public class RatingActualizadoEvent {

    private Long id;
    private int rating;
    private long totalRatings;

    // Constructor sin argumentos requerido para deserialización JSON
    public RatingActualizadoEvent() {
    }

    public RatingActualizadoEvent(Long id, int rating, long totalRatings) {
        this.id = id;
        this.rating = rating;
        this.totalRatings = totalRatings;
    }

    // Getters siguiendo el estilo del proyecto
    public Long id() {
        return id;
    }

    public int rating() {
        return rating;
    }

    public long totalRatings() {
        return totalRatings;
    }

    // Setters para deserialización JSON
    public void setId(Long id) {
        this.id = id;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public void setTotalRatings(long totalRatings) {
        this.totalRatings = totalRatings;
    }

    @Override
    public String toString() {
        return "RatingActualizadoEvent{" +
                "id=" + id +
                ", rating=" + rating +
                ", totalRatings=" + totalRatings +
                '}';
    }
}
