package com.rentacar.presentation.exception;

import com.rentacar.domain.exception.BranchNotFoundException;
import com.rentacar.domain.exception.CustomerNotFoundException;
import com.rentacar.domain.exception.DuplicateDriverLicenseException;
import com.rentacar.domain.exception.DuplicateEmailException;
import com.rentacar.domain.exception.DuplicateLicensePlateException;
import com.rentacar.domain.exception.ExpiredVerificationTokenException;
import com.rentacar.domain.exception.InvalidDriverLicenseException;
import com.rentacar.domain.exception.InvalidEmailException;
import com.rentacar.domain.exception.InvalidLicensePlateException;
import com.rentacar.domain.exception.InvalidMileageException;
import com.rentacar.domain.exception.InvalidRentalAgreementDataException;
import com.rentacar.domain.exception.InvalidVehicleDataException;
import com.rentacar.domain.exception.InvalidVerificationTokenException;
import com.rentacar.domain.exception.TooManyLoginAttemptsException;
import com.rentacar.domain.exception.VehicleNotAvailableException;
import com.rentacar.domain.exception.VehicleNotFoundException;
import com.rentacar.domain.exception.VehicleStatusTransitionException;
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
        ex.getBindingResult().getAllErrors().forEach(error -> {
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
            .toList();

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
     * Behandelt TooManyLoginAttemptsException.
     *
     * @param ex die Exception
     * @return HTTP 429 Too Many Requests mit Retry-After Header
     */
    @ExceptionHandler(TooManyLoginAttemptsException.class)
    public ResponseEntity<ErrorResponse> handleTooManyLoginAttemptsException(TooManyLoginAttemptsException ex) {
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.TOO_MANY_REQUESTS.value(),
            "Zu viele Login-Versuche",
            ex.getMessage()
        );

        return ResponseEntity
            .status(HttpStatus.TOO_MANY_REQUESTS)
            .header("Retry-After", String.valueOf(ex.getRetryAfterSeconds()))
            .body(error);
    }

    /**
     * Behandelt IllegalArgumentException (z.B. ungültige Eingaben).
     * 
     * @param ex die Exception
     * @return HTTP 400 Bad Request
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
     * Behandelt IllegalStateException (z.B. ungültiger Status).
     * 
     * @param ex die Exception
     * @return HTTP 400 Bad Request
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException ex) {
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Ungültiger Status",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
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

    /**
     * Behandelt AccessDeniedException (403 Forbidden).
     * Wird geworfen, wenn ein authentifizierter Benutzer versucht, auf eine Ressource zuzugreifen,
     * für die er keine Berechtigung hat (z.B. CUSTOMER versucht Fahrzeug zu erstellen).
     * 
     * @param ex die Exception
     * @return HTTP 403 Forbidden
     */
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            org.springframework.security.access.AccessDeniedException ex) {
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.FORBIDDEN.value(),
            "Zugriff verweigert",
            "Sie haben nicht die erforderlichen Berechtigungen für diese Aktion."
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    /**
     * Behandelt AuthenticationException (401 Unauthorized).
     * Wird geworfen, wenn Authentifizierung fehlschlägt oder Token ungültig ist.
     * 
     * @param ex die Exception
     * @return HTTP 401 Unauthorized
     */
    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            org.springframework.security.core.AuthenticationException ex) {
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.UNAUTHORIZED.value(),
            "Authentifizierung fehlgeschlagen",
            "Ungültige Anmeldedaten. Bitte überprüfen Sie Ihre E-Mail und Ihr Passwort."
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    /**
     * Behandelt NullPointerException.
     * Wird geworfen, wenn erforderliche Daten null sind (z.B. in VehicleCondition).
     * 
     * @param ex die Exception
     * @return HTTP 400 Bad Request
     */
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ErrorResponse> handleNullPointerException(NullPointerException ex) {
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Ungültige Eingabedaten",
            ex.getMessage() != null ? ex.getMessage() : "Ein erforderliches Feld darf nicht leer sein."
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Behandelt InvalidRentalAgreementDataException.
     * Wird geworfen, wenn Daten für einen Mietvertrag ungültig sind.
     * 
     * @param ex die Exception
     * @return HTTP 400 Bad Request
     */
    @ExceptionHandler(InvalidRentalAgreementDataException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRentalAgreementDataException(
            InvalidRentalAgreementDataException ex) {
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Ungültige Mietvertragsdaten",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
