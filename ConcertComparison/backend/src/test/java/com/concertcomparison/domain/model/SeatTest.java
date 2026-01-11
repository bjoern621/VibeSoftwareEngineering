package com.concertcomparison.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * Domain Tests für Seat Aggregate Root.
 * Testet Business Logic und State Transitions gemäß DDD Best Practices.
 */
class SeatTest {

    private static final Long CONCERT_ID = 1L;
    private static final String SEAT_NUMBER = "A-1";
    private static final String CATEGORY = "VIP";
    private static final String BLOCK = "Block A";
    private static final String ROW = "1";
    private static final String NUMBER = "1";
    private static final Double PRICE = 129.99;

    @Test
    @DisplayName("Neuer Seat sollte AVAILABLE Status haben")
    void newSeat_ShouldHaveAvailableStatus() {
        // Act
        Seat seat = new Seat(CONCERT_ID, SEAT_NUMBER, CATEGORY, BLOCK, ROW, NUMBER, PRICE);

        // Assert
        assertThat(seat.getStatus()).isEqualTo(SeatStatus.AVAILABLE);
        assertThat(seat.getConcertId()).isEqualTo(CONCERT_ID);
        assertThat(seat.getSeatNumber()).isEqualTo(SEAT_NUMBER);
        assertThat(seat.getCategory()).isEqualTo(CATEGORY);
        assertThat(seat.getBlock()).isEqualTo(BLOCK);
        assertThat(seat.getRow()).isEqualTo(ROW);
        assertThat(seat.getNumber()).isEqualTo(NUMBER);
        assertThat(seat.getPrice()).isEqualTo(PRICE);
        assertThat(seat.getHoldReservationId()).isNull();
        assertThat(seat.getHoldExpiresAt()).isNull();
    }

    @Test
    @DisplayName("Constructor sollte null concertId ablehnen")
    void constructor_ShouldRejectNullConcertId() {
        // Act & Assert
        assertThatThrownBy(() -> new Seat(null, SEAT_NUMBER, CATEGORY, BLOCK, ROW, NUMBER, PRICE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Concert-ID");
    }

    @Test
    @DisplayName("Constructor sollte leeren seatNumber ablehnen")
    void constructor_ShouldRejectEmptySeatNumber() {
        // Act & Assert
        assertThatThrownBy(() -> new Seat(CONCERT_ID, "", CATEGORY, BLOCK, ROW, NUMBER, PRICE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Sitzplatznummer");
    }

    @Test
    @DisplayName("Constructor sollte leere category ablehnen")
    void constructor_ShouldRejectEmptyCategory() {
        // Act & Assert
        assertThatThrownBy(() -> new Seat(CONCERT_ID, SEAT_NUMBER, "", BLOCK, ROW, NUMBER, PRICE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Kategorie");
    }

    @Test
    @DisplayName("Constructor sollte leeren block ablehnen")
    void constructor_ShouldRejectEmptyBlock() {
        // Act & Assert
        assertThatThrownBy(() -> new Seat(CONCERT_ID, SEAT_NUMBER, CATEGORY, "", ROW, NUMBER, PRICE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Block");
    }

    @Test
    @DisplayName("Constructor sollte leere row ablehnen")
    void constructor_ShouldRejectEmptyRow() {
        // Act & Assert
        assertThatThrownBy(() -> new Seat(CONCERT_ID, SEAT_NUMBER, CATEGORY, BLOCK, "", NUMBER, PRICE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Reihe");
    }

    @Test
    @DisplayName("Constructor sollte leere number ablehnen")
    void constructor_ShouldRejectEmptyNumber() {
        // Act & Assert
        assertThatThrownBy(() -> new Seat(CONCERT_ID, SEAT_NUMBER, CATEGORY, BLOCK, ROW, "", PRICE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Sitznummer");
    }

    @Test
    @DisplayName("Constructor sollte null price ablehnen")
    void constructor_ShouldRejectNullPrice() {
        // Act & Assert
        assertThatThrownBy(() -> new Seat(CONCERT_ID, SEAT_NUMBER, CATEGORY, BLOCK, ROW, NUMBER, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Preis");
    }

    @Test
    @DisplayName("Constructor sollte negative price ablehnen")
    void constructor_ShouldRejectNegativePrice() {
        // Act & Assert
        assertThatThrownBy(() -> new Seat(CONCERT_ID, SEAT_NUMBER, CATEGORY, BLOCK, ROW, NUMBER, -10.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Preis");
    }

    @Test
    @DisplayName("hold() sollte Seat von AVAILABLE zu HELD ändern")
    void hold_ShouldChangeStatusFromAvailableToHeld() {
        // Arrange
        Seat seat = new Seat(CONCERT_ID, SEAT_NUMBER, CATEGORY, BLOCK, ROW, NUMBER, PRICE);
        String reservationId = "res-123";
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(10);

        // Act
        seat.hold(reservationId, expiresAt);

        // Assert
        assertThat(seat.getStatus()).isEqualTo(SeatStatus.HELD);
        assertThat(seat.getHoldReservationId()).isEqualTo(reservationId);
        assertThat(seat.getHoldExpiresAt()).isEqualTo(expiresAt);
    }

    @Test
    @DisplayName("hold() sollte exception werfen wenn Seat nicht AVAILABLE ist")
    void hold_ShouldThrowException_WhenSeatNotAvailable() {
        // Arrange
        Seat seat = new Seat(CONCERT_ID, SEAT_NUMBER, CATEGORY, BLOCK, ROW, NUMBER, PRICE);
        seat.hold("res-1", LocalDateTime.now().plusMinutes(10));

        // Act & Assert
        assertThatThrownBy(() -> seat.hold("res-2", LocalDateTime.now().plusMinutes(10)))
                .isInstanceOf(com.concertcomparison.domain.exception.SeatNotAvailableException.class)
                .hasMessageContaining("reserviert");
    }

    @Test
    @DisplayName("sell() sollte Seat von HELD zu SOLD ändern und Hold-Daten löschen")
    void sell_ShouldChangeStatusFromHeldToSold_AndClearHoldData() {
        // Arrange
        Seat seat = new Seat(CONCERT_ID, SEAT_NUMBER, CATEGORY, BLOCK, ROW, NUMBER, PRICE);
        String reservationId = "res-123";
        seat.hold(reservationId, LocalDateTime.now().plusMinutes(10));

        // Act
        seat.sell(reservationId);

        // Assert
        assertThat(seat.getStatus()).isEqualTo(SeatStatus.SOLD);
        assertThat(seat.getHoldReservationId()).isNull();
        assertThat(seat.getHoldExpiresAt()).isNull();
    }

    @Test
    @DisplayName("sell() sollte exception werfen bei falscher reservationId")
    void sell_ShouldThrowException_WhenReservationIdDoesNotMatch() {
        // Arrange
        Seat seat = new Seat(CONCERT_ID, SEAT_NUMBER, CATEGORY, BLOCK, ROW, NUMBER, PRICE);
        seat.hold("res-123", LocalDateTime.now().plusMinutes(10));

        // Act & Assert
        assertThatThrownBy(() -> seat.sell("wrong-id"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Reservierungs-ID");
    }

    @Test
    @DisplayName("sell() sollte exception werfen wenn Seat nicht HELD ist")
    void sell_ShouldThrowException_WhenSeatNotHeld() {
        // Arrange
        Seat seat = new Seat(CONCERT_ID, SEAT_NUMBER, CATEGORY, BLOCK, ROW, NUMBER, PRICE);

        // Act & Assert
        assertThatThrownBy(() -> seat.sell("res-123"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nicht reserviert");
    }

    @Test
    @DisplayName("releaseHold() sollte Seat von HELD zu AVAILABLE ändern und Hold-Daten löschen")
    void releaseHold_ShouldChangeStatusFromHeldToAvailable_AndClearHoldData() {
        // Arrange
        Seat seat = new Seat(CONCERT_ID, SEAT_NUMBER, CATEGORY, BLOCK, ROW, NUMBER, PRICE);
        seat.hold("res-123", LocalDateTime.now().plusMinutes(10));

        // Act
        seat.releaseHold();

        // Assert
        assertThat(seat.getStatus()).isEqualTo(SeatStatus.AVAILABLE);
        assertThat(seat.getHoldReservationId()).isNull();
        assertThat(seat.getHoldExpiresAt()).isNull();
    }

    @Test
    @DisplayName("releaseHold() sollte exception werfen wenn Seat nicht HELD ist")
    void releaseHold_ShouldThrowException_WhenSeatNotHeld() {
        // Arrange
        Seat seat = new Seat(CONCERT_ID, SEAT_NUMBER, CATEGORY, BLOCK, ROW, NUMBER, PRICE);

        // Act & Assert
        assertThatThrownBy(() -> seat.releaseHold())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nicht reserviert");
    }

    @Test
    @DisplayName("isHoldExpired() sollte true zurückgeben wenn Hold abgelaufen ist")
    void isHoldExpired_ShouldReturnTrue_WhenHoldExpired() {
        // Arrange
        Seat seat = new Seat(CONCERT_ID, SEAT_NUMBER, CATEGORY, BLOCK, ROW, NUMBER, PRICE);
        seat.hold("res-123", LocalDateTime.now().minusSeconds(1));

        // Act & Assert
        assertThat(seat.isHoldExpired()).isTrue();
    }

    @Test
    @DisplayName("isHoldExpired() sollte false zurückgeben wenn Hold noch gültig ist")
    void isHoldExpired_ShouldReturnFalse_WhenHoldNotExpired() {
        // Arrange
        Seat seat = new Seat(CONCERT_ID, SEAT_NUMBER, CATEGORY, BLOCK, ROW, NUMBER, PRICE);
        seat.hold("res-123", LocalDateTime.now().plusMinutes(10));

        // Act & Assert
        assertThat(seat.isHoldExpired()).isFalse();
    }

    @Test
    @DisplayName("isHoldExpired() sollte false zurückgeben wenn kein Hold vorhanden ist")
    void isHoldExpired_ShouldReturnFalse_WhenNotHeld() {
        // Arrange
        Seat seat = new Seat(CONCERT_ID, SEAT_NUMBER, CATEGORY, BLOCK, ROW, NUMBER, PRICE);

        // Act & Assert
        assertThat(seat.isHoldExpired()).isFalse();
    }
}
