package unrn.service;

/**
 * Excepcion para conflictos de concurrencia al modificar stock de una pelicula.
 *
 * Se lanza cuando la version enviada por el cliente no coincide con la version
 * persistida. Esa diferencia avisa que alguien cambio el stock antes y que el
 * cliente debe refrescar datos antes de volver a intentar.
 */
public class PeliculaStockVersionConflictException extends RuntimeException {
    private final Long peliculaId;
    private final long expectedVersion;
    private final long currentVersion;

    /**
     * Inicializa una instancia de PeliculaStockVersionConflictException con los datos necesarios.
     */
    public PeliculaStockVersionConflictException(Long peliculaId, long expectedVersion, long currentVersion) {
        super("El stock de la pelicula fue modificado por otro proceso");
        this.peliculaId = peliculaId;
        this.expectedVersion = expectedVersion;
        this.currentVersion = currentVersion;
    }

    /**
     * Devuelve el valor de peliculaId.
     */
    public Long peliculaId() {
        return peliculaId;
    }

    /**
     * Devuelve el valor de expectedVersion.
     */
    public long expectedVersion() {
        return expectedVersion;
    }

    /**
     * Devuelve el valor de currentVersion.
     */
    public long currentVersion() {
        return currentVersion;
    }
}
