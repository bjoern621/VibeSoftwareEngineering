package com.travelreimburse.presentation.controller;

import com.travelreimburse.application.service.InvalidFileException;
import com.travelreimburse.domain.exception.AbsenceConflictException;
import com.travelreimburse.domain.exception.DestinationNotFoundException;
import com.travelreimburse.domain.exception.EmployeeNotFoundException;
import com.travelreimburse.domain.exception.InsufficientPermissionException;
import com.travelreimburse.domain.exception.InvalidCountryCodeException;
import com.travelreimburse.domain.exception.InvalidTravelRequestDataException;
import com.travelreimburse.domain.exception.InvalidTravelRequestStateException;
import com.travelreimburse.domain.exception.InsufficientVisaProcessingTimeException;
import com.travelreimburse.domain.exception.ReceiptNotFoundException;
import com.travelreimburse.domain.exception.TravelRequestNotFoundException;
import com.travelreimburse.infrastructure.external.exrat.ExRatClientException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Globaler Exception Handler für REST-Endpunkte
 * Wandelt Exceptions in saubere HTTP-Responses um
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * Behandelt TravelRequestNotFoundException (404)
     */
    @ExceptionHandler(TravelRequestNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTravelRequestNotFound(TravelRequestNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            ex.getMessage(),
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Behandelt ReceiptNotFoundException (404)
     */
    @ExceptionHandler(ReceiptNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleReceiptNotFound(ReceiptNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            ex.getMessage(),
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Behandelt EmployeeNotFoundException (404)
     */
    @ExceptionHandler(EmployeeNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEmployeeNotFound(EmployeeNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            ex.getMessage(),
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    /**
     * Behandelt InvalidFileException (400)
     */
    @ExceptionHandler(InvalidFileException.class)
    public ResponseEntity<ErrorResponse> handleInvalidFile(InvalidFileException ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            ex.getMessage(),
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Behandelt Validierungsfehler (400)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage())
        );
        
        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Validierungsfehler",
            LocalDateTime.now(),
            errors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    /**
     * Behandelt IllegalArgumentException und IllegalStateException (400)
     * DEPRECATED: Wird durch Domain-spezifische Exceptions ersetzt
     */
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ErrorResponse> handleBadRequest(RuntimeException ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            ex.getMessage(),
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    /**
     * Behandelt InvalidTravelRequestStateException (409 - Conflict)
     * Zustandsübergang ist im aktuellen Status nicht erlaubt
     */
    @ExceptionHandler(InvalidTravelRequestStateException.class)
    public ResponseEntity<ErrorResponse> handleInvalidState(InvalidTravelRequestStateException ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.CONFLICT.value(),
            ex.getMessage(),
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
    
    /**
     * Behandelt InvalidTravelRequestDataException (400 - Bad Request)
     * Ungültige Eingabedaten bei Erstellung/Update
     */
    @ExceptionHandler(InvalidTravelRequestDataException.class)
    public ResponseEntity<ErrorResponse> handleInvalidData(InvalidTravelRequestDataException ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            ex.getMessage(),
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    /**
     * Behandelt ExRatClientException (503 - Service Unavailable)
     */
    @ExceptionHandler(ExRatClientException.class)
    public ResponseEntity<ErrorResponse> handleExRatClientException(ExRatClientException ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.SERVICE_UNAVAILABLE.value(),
            "Währungskurs-Service nicht verfügbar: " + ex.getMessage(),
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    /**
     * Behandelt DestinationNotFoundException (404)
     */
    @ExceptionHandler(DestinationNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleDestinationNotFound(DestinationNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            ex.getMessage(),
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Behandelt InvalidCountryCodeException (400)
     */
    @ExceptionHandler(InvalidCountryCodeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCountryCode(InvalidCountryCodeException ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            ex.getMessage(),
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Behandelt InsufficientVisaProcessingTimeException (400)
     */
    @ExceptionHandler(InsufficientVisaProcessingTimeException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientVisaProcessingTime(
        InsufficientVisaProcessingTimeException ex
    ) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            ex.getMessage(),
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Behandelt AbsenceConflictException (409 Conflict)
     * Wird geworfen wenn Reiseantrag mit Abwesenheiten kollidiert
     */
    @ExceptionHandler(AbsenceConflictException.class)
    public ResponseEntity<ErrorResponse> handleAbsenceConflict(AbsenceConflictException ex) {
        Map<String, String> conflictDetails = new HashMap<>();
        
        // Formatiere jeden Konflikt als Detail-Eintrag
        for (int i = 0; i < ex.getConflicts().size(); i++) {
            var conflict = ex.getConflicts().get(i);
            String key = "konflikt_" + (i + 1);
            String value = String.format("%s: %s bis %s (%s)",
                conflict.getType(),
                conflict.getPeriod().getStartDate(),
                conflict.getPeriod().getEndDate(),
                conflict.getReason() != null ? conflict.getReason() : "Keine Angabe"
            );
            conflictDetails.put(key, value);
        }
        
        ErrorResponse error = new ErrorResponse(
            HttpStatus.CONFLICT.value(),
            ex.getMessage(),
            LocalDateTime.now(),
            conflictDetails
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
    
    /**
     * Behandelt InsufficientPermissionException (403)
     */
    @ExceptionHandler(InsufficientPermissionException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientPermission(InsufficientPermissionException ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.FORBIDDEN.value(),
            ex.getMessage(),
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    /**
     * Behandelt alle anderen Exceptions (500)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericError(Exception ex) {
        // Log die echte Exception für Debugging
        ex.printStackTrace();
        System.err.println("UNERWARTETER FEHLER: " + ex.getClass().getName() + ": " + ex.getMessage());
        
        ErrorResponse error = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Ein interner Fehler ist aufgetreten: " + ex.getMessage(),
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
    
    /**
     * Record für Error-Responses
     */
    public record ErrorResponse(
        int status,
        String message,
        LocalDateTime timestamp,
        Map<String, String> details
    ) {
        public ErrorResponse(int status, String message, LocalDateTime timestamp) {
            this(status, message, timestamp, null);
        }
    }
}
