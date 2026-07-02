package unrn.event;

/**
 * Mensaje recibido desde la vertical Rating cuando cambian los agregados de rating.
 *
 * Transporta el id de pelicula, el rating promedio y el total de ratings para que
 * catalogo mantenga sus datos de lectura sincronizados. Se mantiene como clase
 * mutable porque la deserializacion JSON necesita constructor vacio y setters.
 */
public class RatingActualizadoEvent {

    private Long id;
    private int rating;
    private long totalRatings;

    /**
     * Constructor sin argumentos requerido para deserializaciÃƒÆ’Ã‚Â³n JSON
     */
    public RatingActualizadoEvent() {
    }

    /**
     * Inicializa una instancia de RatingActualizadoEvent con los datos necesarios.
     */
    public RatingActualizadoEvent(Long id, int rating, long totalRatings) {
        this.id = id;
        this.rating = rating;
        this.totalRatings = totalRatings;
    }

    /**
     * Devuelve el identificador de la pelicula cuyo rating fue actualizado.
     */
    public Long id() {
        return id;
    }

    /**
     * Devuelve el valor de rating.
     */
    public int rating() {
        return rating;
    }

    /**
     * Devuelve el valor de totalRatings.
     */
    public long totalRatings() {
        return totalRatings;
    }

    /**
     * Setters para deserializaciÃƒÆ’Ã‚Â³n JSON
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Actualiza el rating promedio recibido desde la vertical Rating.
     */
    public void setRating(int rating) {
        this.rating = rating;
    }

    /**
     * Actualiza la cantidad total de ratings recibida desde la vertical Rating.
     */
    public void setTotalRatings(long totalRatings) {
        this.totalRatings = totalRatings;
    }

    /**
     * Devuelve la representacion textual del objeto.
     */
    @Override
    public String toString() {
        return "RatingActualizadoEvent{" +
                "id=" + id +
                ", rating=" + rating +
                ", totalRatings=" + totalRatings +
                '}';
    }
}
