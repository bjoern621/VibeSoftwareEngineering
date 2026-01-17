package com.concertcomparison.application.service;

import com.concertcomparison.domain.model.*;
import com.concertcomparison.domain.repository.OrderRepository;
import com.concertcomparison.domain.repository.ReservationRepository;
import com.concertcomparison.domain.repository.SeatRepository;
import com.concertcomparison.presentation.dto.OrderResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * Unit Tests für OrderApplicationService.
 * 
 * <p>Testet die Purchase-Business-Logik mit Mocks.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderApplicationService Tests")
class OrderApplicationServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private SeatRepository seatRepository;

    @InjectMocks
    private OrderApplicationService orderApplicationService;

    private static final Long RESERVATION_ID = 1L;
    private static final Long SEAT_ID = 100L;
    private static final Long CONCERT_ID = 10L;
    private static final String USER_ID = "user123";
    private static final Double PRICE = 99.99;

    private Reservation activeReservation;
    private Seat heldSeat;
    private Order savedOrder;

    @BeforeEach
    void setUp() {
        // Aktive Reservation erstellen
        activeReservation = Reservation.createHold(SEAT_ID, USER_ID, 15);
        // Reflection-Hack um ID zu setzen (da createHold keine ID setzt)
        setField(activeReservation, "id", RESERVATION_ID);

        // Seat im HELD-Status
        heldSeat = new Seat(CONCERT_ID, "A-1", "VIP", "Block A", "1", "1", PRICE);
        setField(heldSeat, "id", SEAT_ID);
        heldSeat.hold(String.valueOf(RESERVATION_ID), 15);

        // Gespeicherte Order (Mock-Response)
        savedOrder = Order.createFromReservation(activeReservation, heldSeat);
        setField(savedOrder, "id", 999L);
    }

    // ========== Success Flow Tests ==========

    @Test
    @DisplayName("purchaseTicket() sollte Order erstellen bei gültiger Reservation")
    void purchaseTicket_ShouldCreateOrder_WhenReservationIsValid() {
        // Arrange
        given(reservationRepository.findById(RESERVATION_ID)).willReturn(Optional.of(activeReservation));
        given(seatRepository.findById(SEAT_ID)).willReturn(Optional.of(heldSeat));
        given(orderRepository.save(any(Order.class))).willReturn(savedOrder);

        // Act
        OrderResponseDTO result = orderApplicationService.purchaseTicket(RESERVATION_ID, USER_ID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getOrderId()).isEqualTo("999");
        assertThat(result.getSeatId()).isEqualTo(String.valueOf(SEAT_ID));
        assertThat(result.getUserId()).isEqualTo(USER_ID);
        assertThat(result.getTotalPrice()).isEqualTo(PRICE);
        assertThat(result.getStatus()).isEqualTo("CONFIRMED");

        // Verify interactions
        verify(reservationRepository).findById(RESERVATION_ID);
        verify(seatRepository).findById(SEAT_ID);
        verify(seatRepository).save(heldSeat);
        verify(reservationRepository).save(activeReservation);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("purchaseTicket() sollte Seat von HELD zu SOLD ändern")
    void purchaseTicket_ShouldChangeSeatStatus_FromHeldToSold() {
        // Arrange
        given(reservationRepository.findById(RESERVATION_ID)).willReturn(Optional.of(activeReservation));
        given(seatRepository.findById(SEAT_ID)).willReturn(Optional.of(heldSeat));
        given(orderRepository.save(any(Order.class))).willReturn(savedOrder);

        // Act
        orderApplicationService.purchaseTicket(RESERVATION_ID, USER_ID);

        // Assert
        assertThat(heldSeat.getStatus()).isEqualTo(SeatStatus.SOLD);
        assertThat(heldSeat.getHoldReservationId()).isNull();
        assertThat(heldSeat.getHoldExpiresAt()).isNull();

        // Verify seat was saved
        ArgumentCaptor<Seat> seatCaptor = ArgumentCaptor.forClass(Seat.class);
        verify(seatRepository).save(seatCaptor.capture());
        assertThat(seatCaptor.getValue().getStatus()).isEqualTo(SeatStatus.SOLD);
    }

    @Test
    @DisplayName("purchaseTicket() sollte Reservation auf PURCHASED setzen")
    void purchaseTicket_ShouldMarkReservation_AsPurchased() {
        // Arrange
        given(reservationRepository.findById(RESERVATION_ID)).willReturn(Optional.of(activeReservation));
        given(seatRepository.findById(SEAT_ID)).willReturn(Optional.of(heldSeat));
        given(orderRepository.save(any(Order.class))).willReturn(savedOrder);

        // Act
        orderApplicationService.purchaseTicket(RESERVATION_ID, USER_ID);

        // Assert
        assertThat(activeReservation.getStatus()).isEqualTo(ReservationStatus.PURCHASED);

        // Verify reservation was saved
        ArgumentCaptor<Reservation> reservationCaptor = ArgumentCaptor.forClass(Reservation.class);
        verify(reservationRepository).save(reservationCaptor.capture());
        assertThat(reservationCaptor.getValue().getStatus()).isEqualTo(ReservationStatus.PURCHASED);
    }

    // ========== Validation Error Tests ==========

    @Test
    @DisplayName("purchaseTicket() sollte Exception werfen wenn Reservation nicht existiert")
    void purchaseTicket_ShouldThrowException_WhenReservationNotFound() {
        // Arrange
        given(reservationRepository.findById(RESERVATION_ID)).willReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> orderApplicationService.purchaseTicket(RESERVATION_ID, USER_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Reservation nicht gefunden");

        // Verify kein weiterer Zugriff
        verify(reservationRepository).findById(RESERVATION_ID);
        verifyNoInteractions(seatRepository);
        verifyNoInteractions(orderRepository);
    }

    @Test
    @DisplayName("purchaseTicket() sollte Exception werfen wenn Reservation nicht aktiv ist")
    void purchaseTicket_ShouldThrowException_WhenReservationNotActive() {
        // Arrange
        activeReservation.expire(); // Setze auf EXPIRED
        given(reservationRepository.findById(RESERVATION_ID)).willReturn(Optional.of(activeReservation));

        // Act & Assert
        assertThatThrownBy(() -> orderApplicationService.purchaseTicket(RESERVATION_ID, USER_ID))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Reservation ist nicht aktiv");

        verify(reservationRepository).findById(RESERVATION_ID);
        verifyNoInteractions(seatRepository);
        verifyNoInteractions(orderRepository);
    }

    @Test
    @DisplayName("purchaseTicket() sollte Exception werfen wenn Reservation abgelaufen ist")
    void purchaseTicket_ShouldThrowException_WhenReservationExpired() {
        // Arrange
        Reservation expiredReservation = Reservation.createHold(SEAT_ID, USER_ID, 15);
        setField(expiredReservation, "id", RESERVATION_ID);
        // Set expiresAt to past time
        setField(expiredReservation, "expiresAt", LocalDateTime.now().minusMinutes(5));

        given(reservationRepository.findById(RESERVATION_ID)).willReturn(Optional.of(expiredReservation));

        // Act & Assert
        assertThatThrownBy(() -> orderApplicationService.purchaseTicket(RESERVATION_ID, USER_ID))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Reservation ist abgelaufen");

        verify(reservationRepository).findById(RESERVATION_ID);
        verifyNoInteractions(seatRepository);
        verifyNoInteractions(orderRepository);
    }

    @Test
    @DisplayName("purchaseTicket() sollte Exception werfen wenn User nicht Owner ist")
    void purchaseTicket_ShouldThrowException_WhenUserIsNotOwner() {
        // Arrange
        given(reservationRepository.findById(RESERVATION_ID)).willReturn(Optional.of(activeReservation));

        // Act & Assert
        assertThatThrownBy(() -> orderApplicationService.purchaseTicket(RESERVATION_ID, "other-user"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Reservation gehört nicht dem angegebenen User");

        verify(reservationRepository).findById(RESERVATION_ID);
        verifyNoInteractions(seatRepository);
        verifyNoInteractions(orderRepository);
    }

    @Test
    @DisplayName("purchaseTicket() sollte Exception werfen wenn Seat nicht existiert")
    void purchaseTicket_ShouldThrowException_WhenSeatNotFound() {
        // Arrange
        given(reservationRepository.findById(RESERVATION_ID)).willReturn(Optional.of(activeReservation));
        given(seatRepository.findById(SEAT_ID)).willReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> orderApplicationService.purchaseTicket(RESERVATION_ID, USER_ID))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Seat nicht gefunden");

        verify(reservationRepository).findById(RESERVATION_ID);
        verify(seatRepository).findById(SEAT_ID);
        verifyNoInteractions(orderRepository);
    }

    @Test
    @DisplayName("purchaseTicket() sollte Exception werfen wenn Seat nicht HELD ist")
    void purchaseTicket_ShouldThrowException_WhenSeatNotHeld() {
        // Arrange
        Seat availableSeat = new Seat(CONCERT_ID, "A-1", "VIP", "Block A", "1", "1", PRICE);
        setField(availableSeat, "id", SEAT_ID);
        // Seat ist AVAILABLE (nicht HELD)

        given(reservationRepository.findById(RESERVATION_ID)).willReturn(Optional.of(activeReservation));
        given(seatRepository.findById(SEAT_ID)).willReturn(Optional.of(availableSeat));

        // Act & Assert
        assertThatThrownBy(() -> orderApplicationService.purchaseTicket(RESERVATION_ID, USER_ID))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nicht reserviert");

        verify(reservationRepository).findById(RESERVATION_ID);
        verify(seatRepository).findById(SEAT_ID);
        verify(seatRepository, never()).save(any());
        verifyNoInteractions(orderRepository);
    }

    // ========== Helper Methods ==========

    /**
     * Reflection-Hilfsmethode um private Felder zu setzen (für Tests).
     */
    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field: " + fieldName, e);
        }
    }
}
