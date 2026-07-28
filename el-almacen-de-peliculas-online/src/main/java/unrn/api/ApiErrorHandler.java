package unrn.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import unrn.service.CatalogoQueryValidationException;
import unrn.service.PeliculaNotFoundException;
import unrn.service.PeliculaStockVersionConflictException;
import unrn.service.ValidationRuntimeException;

import java.time.Instant;
import java.util.Map;

/**
 * Manejador centralizado de errores HTTP de la API de catalogo.
 *
 * Convierte excepciones de negocio, recursos inexistentes, conflictos de stock y
 * fallas inesperadas en respuestas consistentes. Tambien registra el nivel de log
 * adecuado para distinguir validaciones esperadas de errores tecnicos.
 */
@RestControllerAdvice
public class ApiErrorHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiErrorHandler.class);

    /**
     * Respuesta de error generica para excepciones que no necesitan detalles de campo.
     *
     * Incluye mensaje, estado HTTP, path informado por Spring y timestamp para que
     * clientes y logs puedan correlacionar la falla.
     */
    public record ApiError(String message, int status, String path, Instant timestamp) {
    }

    /**
     * Respuesta de error estructurada para validaciones de negocio.
     *
     * El codigo permite que el frontend tome decisiones sin parsear texto y details
     * conserva datos concretos de la regla que fallo, como campo, valor o version.
     */
    public record ValidationError(String code, String message, Object details) {
    }

    /**
     * Transforma argumentos invalidos en una respuesta HTTP 400.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleBadRequest(IllegalArgumentException ex,
            org.springframework.web.context.request.WebRequest req) {
        log.warn("Bad request: {}", ex.getMessage(), ex);
        var err = new ApiError(ex.getMessage(), HttpStatus.BAD_REQUEST.value(), req.getDescription(false),
                Instant.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
    }

    /**
     * Transforma errores de recurso no encontrado en una respuesta HTTP 404.
     */
    @ExceptionHandler(NotFound.class)
    public ResponseEntity<ApiError> handleNotFound(NotFound ex,
            org.springframework.web.context.request.WebRequest req) {
        log.info("Not found: {}", ex.getMessage());
        var err = new ApiError(ex.getMessage(), HttpStatus.NOT_FOUND.value(), req.getDescription(false), Instant.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(err);
    }

    /**
     * Transforma ausencia de pelicula en una respuesta HTTP 404.
     */
    @ExceptionHandler(PeliculaNotFoundException.class)
    public ResponseEntity<ApiError> handlePeliculaNotFound(PeliculaNotFoundException ex,
            org.springframework.web.context.request.WebRequest req) {
        log.info("Movie not found: {}", ex.getMessage());
        var err = new ApiError(ex.getMessage(), HttpStatus.NOT_FOUND.value(), req.getDescription(false), Instant.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(err);
    }

    /**
     * Transforma errores de validacion de negocio en una respuesta HTTP 400.
     */
    @ExceptionHandler(ValidationRuntimeException.class)
    public ResponseEntity<ApiError> handleValidation(ValidationRuntimeException ex,
            org.springframework.web.context.request.WebRequest req) {
        log.warn("Validation error: {}", ex.getMessage());
        var err = new ApiError(ex.getMessage(), HttpStatus.BAD_REQUEST.value(), req.getDescription(false),
                Instant.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
    }

    /**
     * Transforma errores de filtros, paginacion u orden en una respuesta HTTP 400.
     */
    @ExceptionHandler(CatalogoQueryValidationException.class)
    public ResponseEntity<ValidationError> handleCatalogoQueryValidation(CatalogoQueryValidationException ex) {
        log.warn("Catalog query validation error: {}", ex.getMessage());
        var err = new ValidationError(ex.code(), ex.getMessage(), ex.details());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
    }

    /**
     * Transforma conflictos de version de stock en una respuesta HTTP 409.
     */
    @ExceptionHandler(PeliculaStockVersionConflictException.class)
    public ResponseEntity<ValidationError> handleStockVersionConflict(PeliculaStockVersionConflictException ex) {
        log.warn("Stock version conflict for movie {}: expected {}, current {}", ex.peliculaId(),
                ex.expectedVersion(), ex.currentVersion());
        var err = new ValidationError(
                "STOCK_VERSION_CONFLICT",
                ex.getMessage(),
                Map.of(
                        "peliculaId", ex.peliculaId(),
                        "expectedVersion", ex.expectedVersion(),
                        "currentVersion", ex.currentVersion()));
        return ResponseEntity.status(HttpStatus.CONFLICT).body(err);
    }

    /**
     * Transforma rutas inexistentes en una respuesta HTTP 404.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> handleNoResource(NoResourceFoundException ex,
            org.springframework.web.context.request.WebRequest req) {
        log.info("No resource found: {}", ex.getMessage());
        var err = new ApiError("Resource not found", HttpStatus.NOT_FOUND.value(), req.getDescription(false),
                Instant.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(err);
    }

    /**
     * Registra errores no previstos y devuelve una respuesta HTTP 500.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnknown(Exception ex,
            org.springframework.web.context.request.WebRequest req) {
        // Log la excepcion compelta para diagonosticar errores 500
        log.error("Unhandled exception while processing request {}: {}", req.getDescription(false), ex.getMessage(),
                ex);
        var err = new ApiError("Internal error", HttpStatus.INTERNAL_SERVER_ERROR.value(), req.getDescription(false),
                Instant.now());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
    }
}

/**
 * Excepcion local para recursos publicos no encontrados desde controladores API.
 *
 * Se usa cuando el borde HTTP necesita expresar un 404 sin mezclar esa ausencia
 * con errores de validacion o fallas tecnicas internas.
 */
class NotFound extends RuntimeException {
    /**
     * Inicializa una instancia de NotFound con los datos necesarios.
     */
    public NotFound(String msg) {
        super(msg);
    }
}
