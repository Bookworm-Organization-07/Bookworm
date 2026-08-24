package com.example.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Turns the exceptions the services throw into JSON the browser can
 * read. Without this, a "Cart is empty" surfaces as a bare 500 with an
 * HTML body and the user sees nothing useful.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleConflict(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleForbidden(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("message", "You do not have access to this."));
    }

    /**
     * Everything above this is an expected, "the caller did something
     * wrong" error, so its message is safe to show as-is. Reaching this
     * handler instead means something we did NOT expect went wrong -
     * logged here (with the full stack trace, including the real
     * underlying cause) precisely because this is the only handler that
     * would otherwise leave zero trace of what actually happened.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleEverythingElse(Exception ex) {
        log.error("Unhandled exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", ex.getMessage() == null
                        ? "Something went wrong."
                        : ex.getMessage()));
    }
}
