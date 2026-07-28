package unrn.service;

import java.util.Map;

/**
 * Excepcion de negocio para errores en consultas de catalogo.
 *
 * Se usa cuando filtros, paginacion, ordenamiento o datos administrativos no
 * respetan las reglas esperadas. Incluye un codigo y detalles estructurados para
 * que la API pueda responder con mensajes de validacion claros.
 */
public class CatalogoQueryValidationException extends RuntimeException {

    private final String code;
    private final Map<String, Object> details;

    /**
     * Inicializa una instancia de CatalogoQueryValidationException con los datos necesarios.
     */
    public CatalogoQueryValidationException(String code, String message, Map<String, Object> details) {
        super(message);
        this.code = code;
        this.details = details;
    }

    /**
     * Devuelve el valor de code.
     */
    public String code() {
        return code;
    }

    /**
     * Devuelve el valor de details.
     */
    public Map<String, Object> details() {
        return details;
    }
}
