package com.concertcomparison.presentation.exception;

import com.concertcomparison.domain.exception.*;
import com.concertcomparison.presentation.dto.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Locale;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit Tests für GlobalExceptionHandler.
 * 
 * Verifies:
 * - Alle Exception Handler existieren und werden aufgerufen
 * - Error Response DTOs werden korrekt gebildet
 * - HTTP Status Codes sind korrekt
 * - Fehlermeldungen werden aus MessageSource geladen
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler handler;

    @Mock
    private HttpServletRequest request;

    @Mock
    private MessageSource messageSource;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(handler, "messageSource", messageSource);
        when(request.getRequestURI()).thenReturn("/api/test");
    }

    // ==================== Domain Exception Tests ====================

    @Test
    @DisplayName("SeatNotAvailableException sollte 409 CONFLICT zurückgeben")
    void testHandleSeatNotAvailable() {
        // Arrange
        SeatNotAvailableException ex = new SeatNotAvailableException("Seat A-1 ist nicht verfügbar");
        when(messageSource.getMessage(anyString(), any(), anyString(), any()))
                .thenReturn("Der Sitzplatz ist leider nicht mehr verfügbar");

        // Act
        ResponseEntity<ErrorResponseDTO> response = handler.handleSeatNotAvailable(ex, request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("SEAT_NOT_AVAILABLE");
        assertThat(response.getBody().getStatus()).isEqualTo(409);
        assertThat(response.getBody().getTimestamp()).isNotNull();
        assertThat(response.getBody().getPath()).isEqualTo("/api/test");
    }

    @Test
    @DisplayName("ReservationExpiredException sollte 409 CONFLICT zurückgeben")
    void testHandleReservationExpired() {
        // Arrange
        ReservationExpiredException ex = new ReservationExpiredException("Reservation ist abgelaufen");
        when(messageSource.getMessage(anyString(), any(), anyString(), any()))
                .thenReturn("Ihre Reservierung ist abgelaufen");

        // Act
        ResponseEntity<ErrorResponseDTO> response = handler.handleReservationExpired(ex, request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().getCode()).isEqualTo("RESERVATION_EXPIRED");
    }

    @Test
    @DisplayName("InvalidConcertDateException sollte 400 BAD_REQUEST zurückgeben")
    void testHandleInvalidConcertDate() {
        // Arrange
        InvalidConcertDateException ex = new InvalidConcertDateException("Datum ist in der Vergangenheit");
        when(messageSource.getMessage(anyString(), any(), anyString(), any()))
                .thenReturn("Das Konzertdatum muss in der Zukunft liegen");

        // Act
        ResponseEntity<ErrorResponseDTO> response = handler.handleInvalidConcertDate(ex, request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getCode()).isEqualTo("INVALID_CONCERT_DATE");
    }

    @Test
    @DisplayName("RateLimitExceededException sollte 429 TOO_MANY_REQUESTS mit Retry-After Header zurückgeben")
    void testHandleRateLimitExceeded() {
        // Arrange
        RateLimitExceededException ex = new RateLimitExceededException(
            "Rate limit exceeded",
            "test-client",
            60
        );
        when(messageSource.getMessage(anyString(), any(), anyString(), any()))
                .thenReturn("Rate Limit überschritten");

        // Act
        ResponseEntity<ErrorResponseDTO> response = handler.handleRateLimitExceeded(ex, request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(response.getBody().getCode()).isEqualTo("RATE_LIMIT_EXCEEDED");
        assertThat(response.getBody().getRetryAfter()).isEqualTo(60);
        assertThat(response.getHeaders().get("Retry-After")).isNotEmpty();
    }

    // ==================== ResourceNotFound Exception Tests ====================

    @Test
    @DisplayName("ConcertNotFoundException sollte 404 NOT_FOUND zurückgeben")
    void testHandleConcertNotFound() {
        // Arrange
        ConcertNotFoundException ex = new ConcertNotFoundException(123L);
        when(messageSource.getMessage(anyString(), any(), anyString(), any()))
                .thenReturn("Das Konzert wurde nicht gefunden");

        // Act
        ResponseEntity<ErrorResponseDTO> response = handler.handleConcertNotFound(ex, request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getCode()).isEqualTo("CONCERT_NOT_FOUND");
        assertThat(response.getBody().getStatus()).isEqualTo(404);
    }

    @Test
    @DisplayName("SeatNotFoundException sollte 404 NOT_FOUND zurückgeben")
    void testHandleSeatNotFound() {
        // Arrange
        SeatNotFoundException ex = new SeatNotFoundException(456L);
        when(messageSource.getMessage(anyString(), any(), anyString(), any()))
                .thenReturn("Der Sitzplatz wurde nicht gefunden");

        // Act
        ResponseEntity<ErrorResponseDTO> response = handler.handleSeatNotFound(ex, request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getCode()).isEqualTo("SEAT_NOT_FOUND");
    }

    @Test
    @DisplayName("ReservationNotFoundException sollte 404 NOT_FOUND zurückgeben")
    void testHandleReservationNotFound() {
        // Arrange
        ReservationNotFoundException ex = new ReservationNotFoundException(789L);
        when(messageSource.getMessage(anyString(), any(), anyString(), any()))
                .thenReturn("Die Reservierung wurde nicht gefunden");

        // Act
        ResponseEntity<ErrorResponseDTO> response = handler.handleReservationNotFound(ex, request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getCode()).isEqualTo("RESERVATION_NOT_FOUND");
    }

    @Test
    @DisplayName("OrderNotFoundException sollte 404 NOT_FOUND zurückgeben")
    void testHandleOrderNotFound() {
        // Arrange
        OrderNotFoundException ex = new OrderNotFoundException(999L);
        when(messageSource.getMessage(anyString(), any(), anyString(), any()))
                .thenReturn("Die Bestellung wurde nicht gefunden");

        // Act
        ResponseEntity<ErrorResponseDTO> response = handler.handleOrderNotFound(ex, request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getCode()).isEqualTo("ORDER_NOT_FOUND");
    }

    // ==================== Concurrency Exception Tests ====================

    @Test
    @DisplayName("OptimisticLockingFailureException sollte 409 CONFLICT zurückgeben")
    void testHandleOptimisticLock() {
        // Arrange
        org.springframework.dao.OptimisticLockingFailureException ex = 
            new org.springframework.dao.OptimisticLockingFailureException("Optimistic lock failed");
        when(messageSource.getMessage(anyString(), any(), anyString(), any()))
                .thenReturn("Der Sitzplatz wurde gerade von jemand anderem gebucht");

        // Act
        ResponseEntity<ErrorResponseDTO> response = handler.handleOptimisticLock(ex, request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().getCode()).isEqualTo("CONCURRENCY_CONFLICT");
    }

    // ==================== Response Structure Tests ====================

    @Test
    @DisplayName("ErrorResponseDTO sollte alle erforderlichen Felder enthalten")
    void testErrorResponseDTOStructure() {
        // Arrange
        SeatNotAvailableException ex = new SeatNotAvailableException("Test");
        when(messageSource.getMessage(anyString(), any(), anyString(), any()))
                .thenReturn("Testmeldung");

        // Act
        ResponseEntity<ErrorResponseDTO> response = handler.handleSeatNotAvailable(ex, request);
        ErrorResponseDTO dto = response.getBody();

        // Assert
        assertThat(dto).isNotNull();
        assertThat(dto.getCode()).isNotBlank();
        assertThat(dto.getMessage()).isNotBlank();
        assertThat(dto.getStatus()).isPositive();
        assertThat(dto.getTimestamp()).isNotNull();
        assertThat(dto.getPath()).isNotBlank();
    }

    @Test
    @DisplayName("Timestamp sollte aktuell sein")
    void testTimestampIsCurrentTime() {
        // Arrange
        SeatNotAvailableException ex = new SeatNotAvailableException("Test");
        when(messageSource.getMessage(anyString(), any(), anyString(), any()))
                .thenReturn("Testmeldung");
        LocalDateTime before = LocalDateTime.now();

        // Act
        ResponseEntity<ErrorResponseDTO> response = handler.handleSeatNotAvailable(ex, request);
        LocalDateTime timestamp = response.getBody().getTimestamp();
        LocalDateTime after = LocalDateTime.now();

        // Assert
        assertThat(timestamp).isBetween(before.minusSeconds(1), after.plusSeconds(1));
    }
}
