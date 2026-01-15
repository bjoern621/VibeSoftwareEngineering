package com.concertcomparison.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Tests für Reservation Entity.
 */
class ReservationTest {

    @Test
    void shouldCreateActiveHold() {
        // When
        Reservation reservation = Reservation.createHold(1L, "user123", 15);

        // Then
        assertNotNull(reservation);
        assertEquals(1L, reservation.getSeatId());
        assertEquals("user123", reservation.getUserId());
        assertEquals(ReservationStatus.ACTIVE, reservation.getStatus());
        assertNotNull(reservation.getExpiresAt());
        assertTrue(reservation.isActive());
        assertFalse(reservation.isExpired());
    }

    @Test
    void shouldCalculateCorrectExpiryTime() {
        // When
        Reservation reservation = Reservation.createHold(1L, "user123", 15);

        // Then
        LocalDateTime expectedExpiry = LocalDateTime.now().plusMinutes(15);
        assertTrue(reservation.getExpiresAt().isAfter(LocalDateTime.now()));
        assertTrue(reservation.getExpiresAt().isBefore(expectedExpiry.plusSeconds(5))); // 5s tolerance
    }

    @Test
    void shouldExpireActiveReservation() {
        // Given
        Reservation reservation = Reservation.createHold(1L, "user123", 15);

        // When
        reservation.expire();

        // Then
        assertEquals(ReservationStatus.EXPIRED, reservation.getStatus());
        assertFalse(reservation.isActive());
    }

    @Test
    void shouldNotExpireNonActiveReservation() {
        // Given
        Reservation reservation = Reservation.createHold(1L, "user123", 15);
        reservation.markAsPurchased();

        // When/Then
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> reservation.expire()
        );
        assertTrue(exception.getMessage().contains("Nur ACTIVE Reservations"));
    }

    @Test
    void shouldMarkReservationAsPurchased() {
        // Given
        Reservation reservation = Reservation.createHold(1L, "user123", 15);

        // When
        reservation.markAsPurchased();

        // Then
        assertEquals(ReservationStatus.PURCHASED, reservation.getStatus());
        assertFalse(reservation.isActive());
    }

    @Test
    void shouldNotPurchaseNonActiveReservation() {
        // Given
        Reservation reservation = Reservation.createHold(1L, "user123", 15);
        reservation.expire();

        // When/Then
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> reservation.markAsPurchased()
        );
        assertTrue(exception.getMessage().contains("Nur ACTIVE Reservations"));
    }

    @Test
    void shouldRejectZeroTtl() {
        assertThrows(
            IllegalArgumentException.class,
            () -> Reservation.createHold(1L, "user123", 0)
        );
    }
}
