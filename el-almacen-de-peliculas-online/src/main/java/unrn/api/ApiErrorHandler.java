package unrn.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class ApiErrorHandler {

    public record ApiError(String message, int status, String path, Instant timestamp) {}

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleBadRequest(IllegalArgumentException ex, org.springframework.web.context.request.WebRequest req) {
        var err = new ApiError(ex.getMessage(), HttpStatus.BAD_REQUEST.value(), req.getDescription(false), Instant.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
    }

    @ExceptionHandler(NotFound.class)
    public ResponseEntity<ApiError> handleNotFound(NotFound ex, org.springframework.web.context.request.WebRequest req) {
        var err = new ApiError(ex.getMessage(), HttpStatus.NOT_FOUND.value(), req.getDescription(false), Instant.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(err);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnknown(Exception ex, org.springframework.web.context.request.WebRequest req) {
        var err = new ApiError("Internal error", HttpStatus.INTERNAL_SERVER_ERROR.value(), req.getDescription(false), Instant.now());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
    }
}

class NotFound extends RuntimeException {
    public NotFound(String msg) { super(msg); }
}
