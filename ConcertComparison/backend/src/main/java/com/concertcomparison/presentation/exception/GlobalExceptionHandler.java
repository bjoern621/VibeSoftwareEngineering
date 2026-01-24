package com.concertcomparison.presentation.exception;

import com.concertcomparison.domain.exception.*;
import com.concertcomparison.presentation.dto.ErrorResponseDTO;
import com.concertcomparison.presentation.dto.ValidationErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Globaler Exception Handler für alle REST Controller.
 * 
 * Verantwortlich für:
 * - Zentralisierte Exception-Behandlung
 * - Mapping von technischen Exceptions zu benutzerfreundlichen Error Responses
 * - Internationalisierung (i18n) von Error Messages
 * - Konsistente Error Response Struktur mit ErrorResponseDTO
 * - Fehlerbehandlung für Domain Exceptions, Validation Errors, Security Errors, etc.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @Autowired
    private MessageSource messageSource;


    // ==================== Domain Exceptions ====================

    /**
     * Behandelt SeatNotAvailableException (Seat ist bereits reserviert/verkauft).
     * HTTP Status: 409 CONFLICT
     */
    @ExceptionHandler(SeatNotAvailableException.class)
    public ResponseEntity<ErrorResponseDTO> handleSeatNotAvailable(
            SeatNotAvailableException ex,
            HttpServletRequest request) {
        Locale locale = LocaleContextHolder.getLocale();
        
        String message = messageSource.getMessage(
                "error.seat.not-available",
                new Object[]{},
                ex.getMessage(),
                locale
        );
        
        ErrorResponseDTO response = ErrorResponseDTO.builder()
                .code("SEAT_NOT_AVAILABLE")
                .message(message)
                .status(HttpStatus.CONFLICT.value())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    /**
     * Behandelt SeatNotHeldException (Versuch, nicht-gehaltenen Seat zu verkaufen).
     * HTTP Status: 409 CONFLICT
     */
    @ExceptionHandler(SeatNotHeldException.class)
    public ResponseEntity<ErrorResponseDTO> handleSeatNotHeld(
            SeatNotHeldException ex,
            HttpServletRequest request) {
        Locale locale = LocaleContextHolder.getLocale();
        
        String message = messageSource.getMessage(
                "error.seat.not-held",
                new Object[]{},
                ex.getMessage(),
                locale
        );
        
        ErrorResponseDTO response = ErrorResponseDTO.builder()
                .code("SEAT_NOT_HELD")
                .message(message)
                .status(HttpStatus.CONFLICT.value())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    /**
     * Behandelt ReservationExpiredException (Hold/Reservation ist abgelaufen).
     * HTTP Status: 409 CONFLICT
     */
    @ExceptionHandler(ReservationExpiredException.class)
    public ResponseEntity<ErrorResponseDTO> handleReservationExpired(
            ReservationExpiredException ex,
            HttpServletRequest request) {
        Locale locale = LocaleContextHolder.getLocale();
        
        String message = messageSource.getMessage(
                "error.reservation.expired",
                new Object[]{},
                ex.getMessage(),
                locale
        );
        
        ErrorResponseDTO response = ErrorResponseDTO.builder()
                .code("RESERVATION_EXPIRED")
                .message(message)
                .status(HttpStatus.CONFLICT.value())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    /**
     * Behandelt InvalidConcertDateException (Konzertdatum in Vergangenheit).
     * HTTP Status: 400 BAD_REQUEST
     */
    @ExceptionHandler(InvalidConcertDateException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidConcertDate(
            InvalidConcertDateException ex,
            HttpServletRequest request) {
        Locale locale = LocaleContextHolder.getLocale();
        
        String message = messageSource.getMessage(
                "error.concert.invalid-date",
                new Object[]{},
                ex.getMessage(),
                locale
        );
        
        ErrorResponseDTO response = ErrorResponseDTO.builder()
                .code("INVALID_CONCERT_DATE")
                .message(message)
                .status(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

        /**
         * Behandelt IllegalStateException (z.B. falscher User für Reservation).
         * HTTP Status: 409 CONFLICT
         */
        @ExceptionHandler(IllegalStateException.class)
        public ResponseEntity<ErrorResponseDTO> handleIllegalState(
                        IllegalStateException ex,
                        HttpServletRequest request) {
                Locale locale = LocaleContextHolder.getLocale();

                String message = messageSource.getMessage(
                                "error.state.invalid",
                                new Object[]{},
                                ex.getMessage(),
                                locale
                );

                ErrorResponseDTO response = ErrorResponseDTO.builder()
                                .code("ILLEGAL_STATE")
                                .message(message)
                                .status(HttpStatus.CONFLICT.value())
                                .timestamp(LocalDateTime.now())
                                .path(request.getRequestURI())
                                .build();

                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

    /**
     * Behandelt RateLimitExceededException.
     * HTTP Status: 429 TOO_MANY_REQUESTS
     */
    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ErrorResponseDTO> handleRateLimitExceeded(
            RateLimitExceededException ex,
            HttpServletRequest request) {
        Locale locale = LocaleContextHolder.getLocale();
        
        String message = messageSource.getMessage(
                "error.rate-limit.exceeded",
                new Object[]{ex.getRetryAfterSeconds()},
                ex.getMessage(),
                locale
        );
        
        ErrorResponseDTO response = ErrorResponseDTO.builder()
                .code("RATE_LIMIT_EXCEEDED")
                .message(message)
                .status(HttpStatus.TOO_MANY_REQUESTS.value())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .retryAfter((int) ex.getRetryAfterSeconds())
                .build();
        
        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", String.valueOf(ex.getRetryAfterSeconds()))
                .body(response);
    }

    /**
     * Behandelt ConcertNotFoundException.
     * HTTP Status: 404 NOT_FOUND
     */
    @ExceptionHandler(ConcertNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleConcertNotFound(
            ConcertNotFoundException ex,
            HttpServletRequest request) {
        Locale locale = LocaleContextHolder.getLocale();
        
        String message = messageSource.getMessage(
                "error.concert.not-found",
                new Object[]{},
                ex.getMessage(),
                locale
        );
        
        ErrorResponseDTO response = ErrorResponseDTO.builder()
                .code("CONCERT_NOT_FOUND")
                .message(message)
                .status(HttpStatus.NOT_FOUND.value())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Behandelt SeatNotFoundException.
     * HTTP Status: 404 NOT_FOUND
     * 
     * Versucht, alternative Sitzplätze in der gleichen Kategorie zu finden.
     */
    @ExceptionHandler(SeatNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleSeatNotFound(
            SeatNotFoundException ex,
            HttpServletRequest request) {
        Locale locale = LocaleContextHolder.getLocale();
        
        String message = messageSource.getMessage(
                "error.seat.not-found",
                new Object[]{},
                ex.getMessage(),
                locale
        );
        
        ErrorResponseDTO.Builder builder = ErrorResponseDTO.builder()
                .code("SEAT_NOT_FOUND")
                .message(message)
                .status(HttpStatus.NOT_FOUND.value())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI());
        
        // Versuche, alternative Sitzplätze zu finden (optional)
        // Dies ist nur möglich, wenn wir concertId und category haben
        // Die Exceptions enthalten diese Info nicht, daher können wir hier nicht helfen
        // Alternative: In SeatNotAvailableException könnten wir alternatives hinzufügen
        
        ErrorResponseDTO response = builder.build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Behandelt ReservationNotFoundException.
     * HTTP Status: 404 NOT_FOUND
     */
    @ExceptionHandler(ReservationNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleReservationNotFound(
            ReservationNotFoundException ex,
            HttpServletRequest request) {
        Locale locale = LocaleContextHolder.getLocale();
        
        String message = messageSource.getMessage(
                "error.reservation.not-found",
                new Object[]{},
                ex.getMessage(),
                locale
        );
        
        ErrorResponseDTO response = ErrorResponseDTO.builder()
                .code("RESERVATION_NOT_FOUND")
                .message(message)
                .status(HttpStatus.NOT_FOUND.value())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Behandelt OrderNotFoundException.
     * HTTP Status: 404 NOT_FOUND
     */
    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleOrderNotFound(
            OrderNotFoundException ex,
            HttpServletRequest request) {
        Locale locale = LocaleContextHolder.getLocale();
        
        String message = messageSource.getMessage(
                "error.order.not-found",
                new Object[]{},
                ex.getMessage(),
                locale
        );
        
        ErrorResponseDTO response = ErrorResponseDTO.builder()
                .code("ORDER_NOT_FOUND")
                .message(message)
                .status(HttpStatus.NOT_FOUND.value())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Behandelt generische ResourceNotFoundException.
     * HTTP Status: 404 NOT_FOUND
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleResourceNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request) {
        Locale locale = LocaleContextHolder.getLocale();
        
        String message = messageSource.getMessage(
                "error.general.not-found",
                new Object[]{},
                ex.getMessage(),
                locale
        );
        
        ErrorResponseDTO response = ErrorResponseDTO.builder()
                .code("RESOURCE_NOT_FOUND")
                .message(message)
                .status(HttpStatus.NOT_FOUND.value())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    // ==================== Concurrency Exceptions ====================

    /**
     * Behandelt OptimisticLockingFailureException (Race Condition).
     * HTTP Status: 409 CONFLICT
     */
    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponseDTO> handleOptimisticLock(
            OptimisticLockingFailureException ex,
            HttpServletRequest request) {
        Locale locale = LocaleContextHolder.getLocale();
        
        String message = messageSource.getMessage(
                "error.concurrency.optimistic-lock",
                new Object[]{},
                "Ein anderer Benutzer hat den Sitzplatz gerade gebucht. Bitte versuchen Sie eine andere Auswahl.",
                locale
        );
        
        ErrorResponseDTO response = ErrorResponseDTO.builder()
                .code("CONCURRENCY_CONFLICT")
                .message(message)
                .status(HttpStatus.CONFLICT.value())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    // ==================== Validation Exceptions ====================

    /**
     * Behandelt MethodArgumentNotValidException (Bean Validation Fehler).
     * HTTP Status: 400 BAD_REQUEST
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponseDTO> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        Locale locale = LocaleContextHolder.getLocale();
        
        List<ValidationErrorResponseDTO.FieldError> fieldErrors = new ArrayList<>();
        
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            String message = messageSource.getMessage(
                    "error.field." + error.getCode(),
                    new Object[]{error.getField()},
                    error.getDefaultMessage(),
                    locale
            );
            
            fieldErrors.add(ValidationErrorResponseDTO.FieldError.builder()
                    .field(error.getField())
                    .rejectedValue(error.getRejectedValue())
                    .message(message)
                    .code(error.getCode())
                    .build());
        });
        
        String mainMessage = messageSource.getMessage(
                "error.validation.failed",
                new Object[]{},
                "Validierung fehlgeschlagen",
                locale
        );
        
        ValidationErrorResponseDTO response = ValidationErrorResponseDTO.builder()
                .code("VALIDATION_ERROR")
                .message(mainMessage)
                .status(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .fieldErrors(fieldErrors)
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Behandelt IllegalArgumentException (generische Validierungsfehler).
     * HTTP Status: 400 BAD_REQUEST
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request) {
        Locale locale = LocaleContextHolder.getLocale();
        
        String message = messageSource.getMessage(
                "error.general.bad-request",
                new Object[]{},
                ex.getMessage(),
                locale
        );
        
        ErrorResponseDTO response = ErrorResponseDTO.builder()
                .code("BAD_REQUEST")
                .message(message)
                .status(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Behandelt MethodArgumentTypeMismatchException (z.B. ungültige ID-Formate).
     * HTTP Status: 400 BAD_REQUEST
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDTO> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {
        Locale locale = LocaleContextHolder.getLocale();
        
        String message = messageSource.getMessage(
                "error.general.bad-request",
                new Object[]{},
                "Ungültiges Format für Parameter: " + ex.getName(),
                locale
        );
        
        ErrorResponseDTO response = ErrorResponseDTO.builder()
                .code("BAD_REQUEST")
                .message(message)
                .status(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // ==================== Security Exceptions ====================

    /**
     * Behandelt BadCredentialsException und UsernameNotFoundException.
     * HTTP Status: 401 UNAUTHORIZED
     */
    @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
    public ResponseEntity<ErrorResponseDTO> handleBadCredentials(
            AuthenticationException ex,
            HttpServletRequest request) {
        Locale locale = LocaleContextHolder.getLocale();
        
        String message = messageSource.getMessage(
                "error.user.invalid-credentials",
                new Object[]{},
                "Ungültige Anmeldedaten",
                locale
        );
        
        ErrorResponseDTO response = ErrorResponseDTO.builder()
                .code("INVALID_CREDENTIALS")
                .message(message)
                .status(HttpStatus.UNAUTHORIZED.value())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * Behandelt allgemeine AuthenticationException.
     * HTTP Status: 401 UNAUTHORIZED
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponseDTO> handleAuthenticationException(
            AuthenticationException ex,
            HttpServletRequest request) {
        Locale locale = LocaleContextHolder.getLocale();
        
        String message = messageSource.getMessage(
                "error.auth.invalid-token",
                new Object[]{},
                "Authentifizierung fehlgeschlagen",
                locale
        );
        
        ErrorResponseDTO response = ErrorResponseDTO.builder()
                .code("AUTHENTICATION_FAILED")
                .message(message)
                .status(HttpStatus.UNAUTHORIZED.value())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * Behandelt AccessDeniedException (Benutzer hat keine Berechtigung).
     * HTTP Status: 403 FORBIDDEN
     */
    @ExceptionHandler({AccessDeniedException.class, SecurityException.class})
    public ResponseEntity<ErrorResponseDTO> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request) {
        Locale locale = LocaleContextHolder.getLocale();
        
        String message = messageSource.getMessage(
                "error.auth.access-denied",
                new Object[]{},
                ex.getMessage(),
                locale
        );
        
        ErrorResponseDTO response = ErrorResponseDTO.builder()
                .code("ACCESS_DENIED")
                .message(message)
                .status(HttpStatus.FORBIDDEN.value())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    // ==================== Generic Exception Handler ====================

    /**
     * Fallback Handler für alle nicht speziell behandelten Exceptions.
     * HTTP Status: 500 INTERNAL_SERVER_ERROR
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGeneric(
            Exception ex,
            HttpServletRequest request) {
        Locale locale = LocaleContextHolder.getLocale();
        
        String message = messageSource.getMessage(
                "error.general.internal-server-error",
                new Object[]{},
                "Ein unerwarteter Fehler ist aufgetreten",
                locale
        );
        
        ErrorResponseDTO response = ErrorResponseDTO.builder()
                .code("INTERNAL_SERVER_ERROR")
                .message(message)
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
