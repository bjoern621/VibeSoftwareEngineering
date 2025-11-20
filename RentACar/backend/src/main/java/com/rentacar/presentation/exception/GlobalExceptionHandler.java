package com.rentacar.presentation.exception;

import com.rentacar.domain.exception.BranchNotFoundException;
import com.rentacar.domain.exception.DuplicateLicensePlateException;
import com.rentacar.domain.exception.VehicleNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Globaler Exception Handler für die REST API.
 * 
 * Fängt Exceptions ab und wandelt sie in einheitliche Fehlerantworten um.
 * Alle Fehlermeldungen sind auf Deutsch gemäß Projektanforderungen.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * Behandelt VehicleNotFoundException.
     * 
     * @param ex die Exception
     * @return HTTP 404 Not Found mit Fehlermeldung
     */
    @ExceptionHandler(VehicleNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleVehicleNotFoundException(VehicleNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.NOT_FOUND.value(),
            "Fahrzeug nicht gefunden",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    /**
     * Behandelt DuplicateLicensePlateException.
     * 
     * @param ex die Exception
     * @return HTTP 400 Bad Request mit Fehlermeldung
     */
    @ExceptionHandler(DuplicateLicensePlateException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateLicensePlateException(DuplicateLicensePlateException ex) {
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Kennzeichen bereits vorhanden",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    /**
     * Behandelt BranchNotFoundException.
     * 
     * @param ex die Exception
     * @return HTTP 404 Not Found mit Fehlermeldung
     */
    @ExceptionHandler(BranchNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleBranchNotFoundException(BranchNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.NOT_FOUND.value(),
            "Filiale nicht gefunden",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    /**
     * Behandelt IllegalArgumentException (z.B. Validierungsfehler aus Domain).
     * 
     * @param ex die Exception
     * @return HTTP 400 Bad Request mit Fehlermeldung
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Ungültige Eingabe",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    /**
     * Behandelt IllegalStateException (z.B. ungültige Statusübergänge).
     * 
     * @param ex die Exception
     * @return HTTP 400 Bad Request mit Fehlermeldung
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException ex) {
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Ungültiger Zustand",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    /**
     * Behandelt Validierungsfehler von Bean Validation (@Valid).
     * 
     * @param ex die Exception
     * @return HTTP 400 Bad Request mit detaillierten Validierungsfehlern
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ValidationErrorResponse errorResponse = new ValidationErrorResponse(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Validierungsfehler",
            "Die Eingabedaten sind ungültig",
            errors
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    /**
     * Behandelt alle anderen unerwarteten Exceptions.
     * 
     * @param ex die Exception
     * @return HTTP 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Interner Serverfehler",
            "Ein unerwarteter Fehler ist aufgetreten: " + ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
    
    /**
     * Standard-Fehlerantwort.
     */
    public static class ErrorResponse {
        private LocalDateTime timestamp;
        private int status;
        private String error;
        private String message;
        
        public ErrorResponse(LocalDateTime timestamp, int status, String error, String message) {
            this.timestamp = timestamp;
            this.status = status;
            this.error = error;
            this.message = message;
        }
        
        // Getter
        public LocalDateTime getTimestamp() { return timestamp; }
        public int getStatus() { return status; }
        public String getError() { return error; }
        public String getMessage() { return message; }
    }
    
    /**
     * Erweiterte Fehlerantwort für Validierungsfehler.
     */
    public static class ValidationErrorResponse extends ErrorResponse {
        private Map<String, String> fieldErrors;
        
        public ValidationErrorResponse(LocalDateTime timestamp, int status, String error, 
                                      String message, Map<String, String> fieldErrors) {
            super(timestamp, status, error, message);
            this.fieldErrors = fieldErrors;
        }
        
        public Map<String, String> getFieldErrors() { return fieldErrors; }
    }
}
