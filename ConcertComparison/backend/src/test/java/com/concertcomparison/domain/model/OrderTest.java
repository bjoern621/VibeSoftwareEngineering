package com.concertcomparison.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit Tests für Order Aggregate Root.
 * 
 * <p>Testet Factory-Methods, Validierung und Invarianten.</p>
 */
@DisplayName("Order Domain Model Tests")
class OrderTest {

    private static final Long SEAT_ID = 1L;
    private static final String USER_ID = "user123";
    private static final Double PRICE = 99.99;

    // ========== Factory Method Tests ==========

    @Test
    @DisplayName("createFromReservation() sollte Order mit korrekten Daten erstellen")
    void createFromReservation_ShouldCreateOrderWithCorrectData() {
        // Arrange
        Reservation reservation = Reservation.createHold(SEAT_ID, USER_ID, 15);
        Seat seat = new Seat(1L, "A-1", "VIP", "Block A", "1", "1", PRICE);

        // Act
        Order order = Order.createFromReservation(reservation, seat);

        // Assert
        assertThat(order).isNotNull();
        assertThat(order.getSeatId()).isEqualTo(SEAT_ID);
        assertThat(order.getUserId()).isEqualTo(USER_ID);
        assertThat(order.getTotalPrice()).isEqualTo(PRICE);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(order.getCreatedAt()).isNotNull();
        assertThat(order.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("createFromReservation() sollte Exception werfen wenn Reservation null ist")
    void createFromReservation_ShouldThrowException_WhenReservationIsNull() {
        // Arrange
        Seat seat = new Seat(1L, "A-1", "VIP", "Block A", "1", "1", PRICE);

        // Act & Assert
        assertThatThrownBy(() -> Order.createFromReservation(null, seat))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Reservation darf nicht null sein");
    }

    @Test
    @DisplayName("createFromReservation() sollte Exception werfen wenn Seat null ist")
    void createFromReservation_ShouldThrowException_WhenSeatIsNull() {
        // Arrange
        Reservation reservation = Reservation.createHold(SEAT_ID, USER_ID, 15);

        // Act & Assert
        assertThatThrownBy(() -> Order.createFromReservation(reservation, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Seat darf nicht null sein");
    }

    // ========== Constructor Validation Tests ==========

    @Test
    @DisplayName("Order sollte Status CONFIRMED haben")
    void order_ShouldHaveStatusConfirmed() {
        // Arrange
        Reservation reservation = Reservation.createHold(SEAT_ID, USER_ID, 15);
        Seat seat = new Seat(1L, "A-1", "VIP", "Block A", "1", "1", PRICE);

        // Act
        Order order = Order.createFromReservation(reservation, seat);

        // Assert
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    @DisplayName("Order sollte createdAt auf aktuelles Datum setzen")
    void order_ShouldSetCreatedAtToNow() {
        // Arrange
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);
        Reservation reservation = Reservation.createHold(SEAT_ID, USER_ID, 15);
        Seat seat = new Seat(1L, "A-1", "VIP", "Block A", "1", "1", PRICE);

        // Act
        Order order = Order.createFromReservation(reservation, seat);
        LocalDateTime after = LocalDateTime.now().plusSeconds(1);

        // Assert
        assertThat(order.getCreatedAt())
                .isAfter(before)
                .isBefore(after);
    }

    // ========== Equals & HashCode Tests ==========

    @Test
    @DisplayName("toString() sollte Order-Informationen enthalten")
    void toString_ShouldContainOrderInformation() {
        // Arrange
        Reservation reservation = Reservation.createHold(SEAT_ID, USER_ID, 15);
        Seat seat = new Seat(1L, "A-1", "VIP", "Block A", "1", "1", PRICE);
        Order order = Order.createFromReservation(reservation, seat);

        // Act
        String result = order.toString();

        // Assert
        assertThat(result)
                .contains("Order{")
                .contains("seatId=" + SEAT_ID)
                .contains("userId='" + USER_ID)
                .contains("totalPrice=" + PRICE)
                .contains("status=CONFIRMED");
    }
}
