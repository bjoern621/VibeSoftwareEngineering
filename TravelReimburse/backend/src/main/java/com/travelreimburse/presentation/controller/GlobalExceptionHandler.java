package com.travelreimburse.presentation.controller;

import com.travelreimburse.application.service.InvalidFileException;
import com.travelreimburse.domain.exception.*;
import com.travelreimburse.infrastructure.external.easypay.EasyPayException;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * Behandelt:
 * - Domain Exceptions (Business-Logik Fehler)
 * - Payment Exceptions (EasyPay Integration)
 * - Validation Exceptions (Input-Validierung)
 * - Service Exceptions (Not Found, Invalid File, etc.)
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ===== PAYMENT EXCEPTION HANDLER =====

    /**
     * Handler für CannotSubmitPaymentException (400)
     */
    @ExceptionHandler(CannotSubmitPaymentException.class)
    public ResponseEntity<ErrorResponse> handleCannotSubmitPayment(CannotSubmitPaymentException ex) {
        logger.warn("CannotSubmitPaymentException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                LocalDateTime.now()
            ));
    }

    /**
     * Handler für EasyPayException (502)
     */
    @ExceptionHandler(EasyPayException.class)
    public ResponseEntity<ErrorResponse> handleEasyPayException(EasyPayException ex) {
        logger.error("EasyPayException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
            .body(new ErrorResponse(
                HttpStatus.BAD_GATEWAY.value(),
                ex.getMessage(),
                LocalDateTime.now()
            ));
    }

    // ===== TRAVEL REQUEST EXCEPTION HANDLER =====

    /**
     * Behandelt TravelRequestNotFoundException (404)
     */
    @ExceptionHandler(TravelRequestNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTravelRequestNotFound(TravelRequestNotFoundException ex) {
        logger.warn("TravelRequestNotFoundException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                LocalDateTime.now()
            ));
    }

    /**
     * Behandelt ReceiptNotFoundException (404)
     */
    @ExceptionHandler(ReceiptNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleReceiptNotFound(ReceiptNotFoundException ex) {
        logger.warn("ReceiptNotFoundException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                LocalDateTime.now()
            ));
    }

    /**
     * Behandelt EmployeeNotFoundException (404)
     */
    @ExceptionHandler(EmployeeNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEmployeeNotFound(EmployeeNotFoundException ex) {
        logger.warn("EmployeeNotFoundException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                LocalDateTime.now()
            ));
    }

    /**
     * Behandelt InvalidFileException (400)
     */
    @ExceptionHandler(InvalidFileException.class)
    public ResponseEntity<ErrorResponse> handleInvalidFile(InvalidFileException ex) {
        logger.warn("InvalidFileException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                LocalDateTime.now()
            ));
    }

    /**
     * Behandelt InvalidTravelRequestStateException (409 - Conflict)
     */
    @ExceptionHandler(InvalidTravelRequestStateException.class)
    public ResponseEntity<ErrorResponse> handleInvalidState(InvalidTravelRequestStateException ex) {
        logger.warn("InvalidTravelRequestStateException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                LocalDateTime.now()
            ));
    }

    /**
     * Behandelt InvalidTravelRequestDataException (400)
     */
    @ExceptionHandler(InvalidTravelRequestDataException.class)
    public ResponseEntity<ErrorResponse> handleInvalidData(InvalidTravelRequestDataException ex) {
        logger.warn("InvalidTravelRequestDataException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                LocalDateTime.now()
            ));
    }

    /**
     * Behandelt CannotArchiveTravelRequestException (400)
     */
    @ExceptionHandler(CannotArchiveTravelRequestException.class)
    public ResponseEntity<ErrorResponse> handleCannotArchive(CannotArchiveTravelRequestException ex) {
        logger.warn("CannotArchiveTravelRequestException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                LocalDateTime.now()
            ));
    }

    // ===== DESTINATION & COUNTRY EXCEPTION HANDLER =====

    /**
     * Behandelt DestinationNotFoundException (404)
     */
    @ExceptionHandler(DestinationNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleDestinationNotFound(DestinationNotFoundException ex) {
        logger.warn("DestinationNotFoundException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                LocalDateTime.now()
            ));
    }

    /**
     * Behandelt InvalidCountryCodeException (400)
     */
    @ExceptionHandler(InvalidCountryCodeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCountryCode(InvalidCountryCodeException ex) {
        logger.warn("InvalidCountryCodeException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                LocalDateTime.now()
            ));
    }

    /**
     * Behandelt InsufficientVisaProcessingTimeException (400)
     */
    @ExceptionHandler(InsufficientVisaProcessingTimeException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientVisaProcessingTime(
        InsufficientVisaProcessingTimeException ex
    ) {
        logger.warn("InsufficientVisaProcessingTimeException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                LocalDateTime.now()
            ));
    }

    /**
     * Behandelt AbsenceConflictException (409 Conflict)
     */
    @ExceptionHandler(AbsenceConflictException.class)
    public ResponseEntity<ErrorResponse> handleAbsenceConflict(AbsenceConflictException ex) {
        logger.warn("AbsenceConflictException: {}", ex.getMessage());
        Map<String, String> conflictDetails = new HashMap<>();

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
        logger.warn("InsufficientPermissionException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(new ErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                ex.getMessage(),
                LocalDateTime.now()
            ));
    }

    // ===== EXTERNAL SERVICE EXCEPTION HANDLER =====

    /**
     * Behandelt ExRatClientException (503)
     */
    @ExceptionHandler(ExRatClientException.class)
    public ResponseEntity<ErrorResponse> handleExRatClientException(ExRatClientException ex) {
        logger.error("ExRatClientException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(new ErrorResponse(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "Währungskurs-Service nicht verfügbar: " + ex.getMessage(),
                LocalDateTime.now()
            ));
    }

    // ===== GENERIC EXCEPTION HANDLER =====

    /**
     * Behandelt Validierungsfehler (400)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        logger.warn("MethodArgumentNotValidException: Validierungsfehler");
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
     */
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ErrorResponse> handleBadRequest(RuntimeException ex) {
        logger.warn("IllegalException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                LocalDateTime.now()
            ));
    }

    /**
     * Behandelt alle anderen Exceptions (500)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericError(Exception ex) {
        logger.error("UNERWARTETER FEHLER: " + ex.getClass().getName(), ex);

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
