package com.travelreimburse.presentation.exception;

import com.travelreimburse.domain.exception.CannotSubmitPaymentException;
import com.travelreimburse.infrastructure.external.easypay.EasyPayException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Global Exception Handler für alle REST Controller
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handler für CannotSubmitPaymentException
     */
    @ExceptionHandler(CannotSubmitPaymentException.class)
    public ResponseEntity<ErrorResponse> handleCannotSubmitPayment(CannotSubmitPaymentException ex) {
        logger.warn("CannotSubmitPaymentException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(
                "PAYMENT_SUBMISSION_NOT_ALLOWED",
                ex.getMessage(),
                LocalDateTime.now()
            ));
    }

    /**
     * Handler für EasyPayException
     */
    @ExceptionHandler(EasyPayException.class)
    public ResponseEntity<ErrorResponse> handleEasyPayException(EasyPayException ex) {
        logger.error("EasyPayException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
            .body(new ErrorResponse(
                ex.getErrorCode(),
                ex.getMessage(),
                LocalDateTime.now()
            ));
    }

    /**
     * Handler für IllegalArgumentException
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        logger.warn("IllegalArgumentException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(
                "INVALID_ARGUMENT",
                ex.getMessage(),
                LocalDateTime.now()
            ));
    }

    /**
     * Handler für IllegalStateException
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex) {
        logger.warn("IllegalStateException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(
                "INVALID_STATE",
                ex.getMessage(),
                LocalDateTime.now()
            ));
    }

    /**
     * Handler für Validation Errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining(", "));

        logger.warn("Validation error: {}", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(
                "VALIDATION_ERROR",
                message,
                LocalDateTime.now()
            ));
    }

    /**
     * Fallback für alle anderen Exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        logger.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse(
                "INTERNAL_SERVER_ERROR",
                "Ein interner Fehler ist aufgetreten",
                LocalDateTime.now()
            ));
    }

    /**
     * DTO für Error Responses
     */
    public record ErrorResponse(
        String code,
        String message,
        LocalDateTime timestamp
    ) {}
}

