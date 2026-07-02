package unrn.service;

/**
 * Excepcion que indica que una pelicula solicitada no existe o no esta disponible.
 *
 * Permite diferenciar una ausencia de catalogo de otros errores de validacion y
 * facilita que la capa API la convierta en una respuesta HTTP 404 consistente.
 */
public class PeliculaNotFoundException extends RuntimeException {
    /**
     * Inicializa una instancia de PeliculaNotFoundException con los datos necesarios.
     */
    public PeliculaNotFoundException(String message) {
        super(message);
    }
}
