package com.concertcomparison.presentation.exception;

import com.concertcomparison.domain.exception.SeatNotAvailableException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

/**
 * Globaler Exception Handler für die Concert Comparison REST API.
 *
 * Behandelt Domain Exceptions, Validation Errors und technische Fehler.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Constants for duplicated literals (SonarQube Best Practice)
    private static final String MESSAGE_KEY = "message";
    private static final String ERRORS_KEY = "errors";
    private static final String VALIDATION_ERROR_CODE = "VALIDATION_ERROR";

    /**
     * Behandelt SeatNotAvailableException (Domain Exception).
     *
     * @param ex Domain Exception
     * @return HTTP 409 Conflict
     */
    @ExceptionHandler(SeatNotAvailableException.class)
    public ResponseEntity<Map<String, String>> handleSeatNotAvailable(SeatNotAvailableException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("code", "SEAT_NOT_AVAILABLE");
        body.put(MESSAGE_KEY, ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    /**
     * Behandelt OptimisticLockingFailureException (Concurrency Conflict).
     *
     * @param ex Optimistic Locking Exception
     * @return HTTP 409 Conflict
     */
    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<Map<String, String>> handleOptimisticLock(OptimisticLockingFailureException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("code", "CONCURRENCY_CONFLICT");
        body.put(MESSAGE_KEY, "Concurrency conflict occurred, please retry");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    /**
     * Behandelt ConstraintViolationException (Bean Validation auf Method-Parametern).
     *
     * Wird geworfen bei Validierungsfehler von @Min, @Max, etc. auf Controller-Parametern.
     *
     * @param ex Constraint Violation Exception
     * @return HTTP 400 Bad Request mit Validierungsfehlern
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("code", VALIDATION_ERROR_CODE);
        body.put(MESSAGE_KEY, "Validierungsfehler in Request-Parametern");

        Map<String, String> errors = new HashMap<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            String propertyPath = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            errors.put(propertyPath, message);
        }
        body.put(ERRORS_KEY, errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * Behandelt MethodArgumentNotValidException (Bean Validation auf Request Body).
     *
     * Wird geworfen bei Validierungsfehler von @Valid auf Request Body DTOs.
     *
     * @param ex Method Argument Not Valid Exception
     * @return HTTP 400 Bad Request mit Validierungsfehlern
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("code", VALIDATION_ERROR_CODE);
        body.put(MESSAGE_KEY, "Validierungsfehler im Request Body");

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        body.put(ERRORS_KEY, errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * Behandelt MethodArgumentTypeMismatchException (Type Conversion Fehler).
     *
     * Wird geworfen wenn ein Query-Parameter nicht in den erwarteten Typ konvertiert werden kann
     * oder wenn @Min/@Max Validierungen fehlschlagen.
     *
     * @param ex Method Argument Type Mismatch Exception
     * @return HTTP 400 Bad Request
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("code", VALIDATION_ERROR_CODE);
        body.put(MESSAGE_KEY, "Ungültiger Wert für Parameter: " + ex.getName());

        Map<String, String> errors = new HashMap<>();
        errors.put(ex.getName(), "Wert '" + ex.getValue() + "' ist ungültig");
        body.put(ERRORS_KEY, errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * Behandelt alle anderen unerwarteten Exceptions.
     *
     * @param ex Generic Exception
     * @return HTTP 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneric(Exception ex) {
        // Log the exception for debugging (SonarQube: Use logger instead of System.err)
        logger.error("Unhandled exception: {} - {}", ex.getClass().getName(), ex.getMessage(), ex);

        Map<String, String> body = new HashMap<>();
        body.put("code", "INTERNAL_ERROR");
        body.put(MESSAGE_KEY, "Ein interner Fehler ist aufgetreten");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
