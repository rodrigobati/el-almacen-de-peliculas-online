package unrn.service;

import java.util.Map;

public class CatalogoQueryValidationException extends RuntimeException {

    private final String code;
    private final Map<String, Object> details;

    public CatalogoQueryValidationException(String code, String message, Map<String, Object> details) {
        super(message);
        this.code = code;
        this.details = details;
    }

    public String code() {
        return code;
    }

    public Map<String, Object> details() {
        return details;
    }
}