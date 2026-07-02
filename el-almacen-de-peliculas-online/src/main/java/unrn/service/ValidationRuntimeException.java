package unrn.service;

/**
 * Excepcion generica para validaciones de negocio que deben devolverse como 400.
 *
 * Se usa en puntos donde la regla no necesita una excepcion especializada, pero
 * igualmente conviene distinguirla de fallas tecnicas o errores inesperados.
 */
public class ValidationRuntimeException extends RuntimeException {
    /**
     * Inicializa una instancia de ValidationRuntimeException con los datos necesarios.
     */
    public ValidationRuntimeException(String message) {
        super(message);
    }
}
