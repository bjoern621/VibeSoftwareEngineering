package com.rentacar.presentation.exception;

import com.rentacar.domain.exception.*;
import com.rentacar.domain.model.Vehicle;
import com.rentacar.presentation.dto.VehicleResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
     * Behandelt InvalidLicensePlateException.
     * 
     * @param ex die Exception
     * @return HTTP 400 Bad Request mit Fehlermeldung
     */
    @ExceptionHandler(InvalidLicensePlateException.class)
    public ResponseEntity<ErrorResponse> handleInvalidLicensePlateException(InvalidLicensePlateException ex) {
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Ungültiges Kennzeichen",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    /**
     * Behandelt InvalidMileageException.
     * 
     * @param ex die Exception
     * @return HTTP 400 Bad Request mit Fehlermeldung
     */
    @ExceptionHandler(InvalidMileageException.class)
    public ResponseEntity<ErrorResponse> handleInvalidMileageException(InvalidMileageException ex) {
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Ungültiger Kilometerstand",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    /**
     * Behandelt VehicleStatusTransitionException.
     * 
     * @param ex die Exception
     * @return HTTP 409 Conflict mit Fehlermeldung
     */
    @ExceptionHandler(VehicleStatusTransitionException.class)
    public ResponseEntity<ErrorResponse> handleVehicleStatusTransitionException(VehicleStatusTransitionException ex) {
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.CONFLICT.value(),
            "Ungültige Statusübergang",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
    
    /**
     * Behandelt InvalidVehicleDataException.
     * 
     * @param ex die Exception
     * @return HTTP 400 Bad Request mit Fehlermeldung
     */
    @ExceptionHandler(InvalidVehicleDataException.class)
    public ResponseEntity<ErrorResponse> handleInvalidVehicleDataException(InvalidVehicleDataException ex) {
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Ungültige Fahrzeugdaten",
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
     * Behandelt CustomerNotFoundException.
     * 
     * @param ex die Exception
     * @return HTTP 404 Not Found mit Fehlermeldung
     */
    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCustomerNotFoundException(CustomerNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.NOT_FOUND.value(),
            "Kunde nicht gefunden",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Behandelt DuplicateEmailException.
     * 
     * @param ex die Exception
     * @return HTTP 400 Bad Request mit Fehlermeldung
     */
    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateEmailException(DuplicateEmailException ex) {
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            "E-Mail bereits registriert",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Behandelt DuplicateDriverLicenseException.
     * 
     * @param ex die Exception
     * @return HTTP 400 Bad Request mit Fehlermeldung
     */
    @ExceptionHandler(DuplicateDriverLicenseException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateDriverLicenseException(DuplicateDriverLicenseException ex) {
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Führerscheinnummer bereits registriert",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Behandelt InvalidVerificationTokenException.
     * 
     * @param ex die Exception
     * @return HTTP 400 Bad Request mit Fehlermeldung
     */
    @ExceptionHandler(InvalidVerificationTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidVerificationTokenException(InvalidVerificationTokenException ex) {
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Ungültiger Verifikations-Token",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Behandelt ExpiredVerificationTokenException.
     * 
     * @param ex die Exception
     * @return HTTP 400 Bad Request mit Fehlermeldung
     */
    @ExceptionHandler(ExpiredVerificationTokenException.class)
    public ResponseEntity<ErrorResponse> handleExpiredVerificationTokenException(ExpiredVerificationTokenException ex) {
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Verifikations-Token abgelaufen",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Behandelt InvalidEmailException.
     * 
     * @param ex die Exception
     * @return HTTP 400 Bad Request mit Fehlermeldung
     */
    @ExceptionHandler(InvalidEmailException.class)
    public ResponseEntity<ErrorResponse> handleInvalidEmailException(InvalidEmailException ex) {
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Ungültige E-Mail-Adresse",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Behandelt InvalidDriverLicenseException.
     * 
     * @param ex die Exception
     * @return HTTP 400 Bad Request mit Fehlermeldung
     */
    @ExceptionHandler(InvalidDriverLicenseException.class)
    public ResponseEntity<ErrorResponse> handleInvalidDriverLicenseException(InvalidDriverLicenseException ex) {
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Ungültige Führerscheinnummer",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    /**
     * Behandelt VehicleNotAvailableException.
     * 
     * @param ex die Exception
     * @return HTTP 409 Conflict mit Fehlermeldung und Alternativen
     */
    @ExceptionHandler(VehicleNotAvailableException.class)
    public ResponseEntity<VehicleNotAvailableErrorResponse> handleVehicleNotAvailableException(VehicleNotAvailableException ex) {
        List<VehicleResponseDTO> alternatives = ex.getAlternativeVehicles().stream()
            .map(VehicleResponseDTO::fromEntity)
            .collect(Collectors.toList());

        VehicleNotAvailableErrorResponse error = new VehicleNotAvailableErrorResponse(
            LocalDateTime.now(),
            HttpStatus.CONFLICT.value(),
            "Fahrzeug nicht verfügbar",
            ex.getMessage(),
            alternatives
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
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

    /**
     * Erweiterte Fehlerantwort für nicht verfügbare Fahrzeuge.
     */
    public static class VehicleNotAvailableErrorResponse extends ErrorResponse {
        private List<VehicleResponseDTO> alternativeVehicles;
        
        public VehicleNotAvailableErrorResponse(LocalDateTime timestamp, int status, String error, 
                                      String message, List<VehicleResponseDTO> alternativeVehicles) {
            super(timestamp, status, error, message);
            this.alternativeVehicles = alternativeVehicles;
        }
        
        public List<VehicleResponseDTO> getAlternativeVehicles() { return alternativeVehicles; }
    }
}
