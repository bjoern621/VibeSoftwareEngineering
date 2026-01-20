package com.concertcomparison.application.service;

import com.concertcomparison.domain.exception.ReservationExpiredException;
import com.concertcomparison.domain.model.*;
import com.concertcomparison.domain.repository.OrderRepository;
import com.concertcomparison.domain.repository.ReservationRepository;
import com.concertcomparison.domain.repository.SeatRepository;
import com.concertcomparison.domain.repository.ConcertRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration Tests für OrderApplicationService (US-03).
 * 
 * Nutzt echte Datenbank (H2 in-memory) und echte Daten.
 * 
 * Test Coverage:
 * - Erfolgreicher Ticket-Kauf
 * - Hold nicht gefunden
 * - Abgelaufener Hold
 * - Hold gehört anderem User
 * - Seat nicht HELD
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("OrderApplicationService Integration Tests")
class OrderApplicationServiceTest {

    @Autowired
    private OrderApplicationService orderApplicationService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private ConcertRepository concertRepository;

    private static final String USER_ID = "test@example.com";
    private static final String OTHER_USER_ID = "other@example.com";

    private Seat testSeat;
    private Concert testConcert;

    @BeforeEach
    void setUp() {
        // Clean up database
        orderRepository.deleteAll();
        reservationRepository.deleteAll();
        seatRepository.deleteAll();
        concertRepository.deleteAll();

        // Create test concert in database
        testConcert = Concert.createConcert(
            "Test Concert",
            LocalDateTime.of(2026, 12, 31, 20, 0),
            "Test Arena",
            "Test Event"
        );
        testConcert = concertRepository.save(testConcert);

        // Create test seat in database (AVAILABLE)
        testSeat = new Seat(
            testConcert.getId(),
            "A-1-VIP",      // seatNumber
            "VIP",          // category
            "A",            // block
            "1",            // row
            "1",            // number
            99.99           // price
        );
        testSeat = seatRepository.save(testSeat);
    }

    // ==================== SUCCESS TEST ====================

    @Test
    @DisplayName("purchaseTicket - Erfolgreicher Kauf")
    void purchaseTicket_Success() {
        // Arrange - Erstelle Hold in der Datenbank
        testSeat.hold(String.valueOf(testSeat.getId()), 15);
        testSeat = seatRepository.save(testSeat);

        Reservation testReservation = Reservation.createHold(testSeat.getId(), USER_ID, 15);
        testReservation = reservationRepository.save(testReservation);

        // Seat mit Reservation-ID aktualisieren
        testSeat.updateHoldReservationId(String.valueOf(testReservation.getId()));
        testSeat = seatRepository.save(testSeat);

        // Act
        Order result = orderApplicationService.purchaseTicket(testReservation.getId(), USER_ID);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getSeatId()).isEqualTo(testSeat.getId());
        assertThat(result.getUserId()).isEqualTo(USER_ID);
        assertThat(result.getTotalPrice()).isEqualTo(99.99);
        assertThat(result.getStatus()).isEqualTo(OrderStatus.CONFIRMED);

        // Verify Database State
        // Seat ist SOLD
        Seat updatedSeat = seatRepository.findById(testSeat.getId()).orElseThrow();
        assertThat(updatedSeat.getStatus()).isEqualTo(SeatStatus.SOLD);

        // Hold wurde gelöscht
        assertThat(reservationRepository.findById(testReservation.getId())).isEmpty();

        // Order wurde erstellt
        assertThat(orderRepository.findById(result.getId())).isPresent();
    }

    // ==================== ERROR TESTS ====================

    @Test
    @DisplayName("purchaseTicket - Hold nicht gefunden")
    void purchaseTicket_ReservationNotFound() {
        // Act & Assert
        assertThatThrownBy(() -> orderApplicationService.purchaseTicket(999L, USER_ID))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Reservation mit ID 999 nicht gefunden");

        // Verify keine Order erstellt
        assertThat(orderRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("purchaseTicket - Abgelaufener Hold")
    void purchaseTicket_ExpiredHold() {
        // Arrange - Erstelle abgelaufenen Hold in DB
        testSeat.hold(String.valueOf(testSeat.getId()), 15);
        testSeat = seatRepository.save(testSeat);

        Reservation testReservation = Reservation.createHold(testSeat.getId(), USER_ID, 15);
        // Setze expiresAt auf Vergangenheit via Reflection
        setExpiresAt(testReservation, LocalDateTime.now().minusMinutes(5));
        Reservation savedReservation = reservationRepository.save(testReservation);

        // Act & Assert
        assertThatThrownBy(() -> orderApplicationService.purchaseTicket(savedReservation.getId(), USER_ID))
            .isInstanceOf(ReservationExpiredException.class)
            .hasMessageContaining("abgelaufen");

        // Verify keine Order erstellt
        assertThat(orderRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("purchaseTicket - Hold gehört anderem User")
    void purchaseTicket_WrongUser() {
        // Arrange
        testSeat.hold(String.valueOf(testSeat.getId()), 15);
        testSeat = seatRepository.save(testSeat);

        Reservation testReservation = Reservation.createHold(testSeat.getId(), USER_ID, 15);
        Reservation savedReservation = reservationRepository.save(testReservation);

        // Act & Assert - Anderer User versucht zu kaufen
        assertThatThrownBy(() -> orderApplicationService.purchaseTicket(savedReservation.getId(), OTHER_USER_ID))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("gehört nicht zum User");

        // Verify Hold noch vorhanden, keine Order erstellt
        assertThat(reservationRepository.findById(savedReservation.getId())).isPresent();
        assertThat(orderRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("purchaseTicket - Seat nicht HELD")
    void purchaseTicket_SeatNotHeld() {
        // Arrange - Reservation existiert, aber Seat ist AVAILABLE (Inkonsistenz)
        Reservation testReservation = Reservation.createHold(testSeat.getId(), USER_ID, 15);
        Reservation savedReservation = reservationRepository.save(testReservation);
        // testSeat bleibt AVAILABLE (nicht hold() aufgerufen)

        // Act & Assert
        assertThatThrownBy(() -> orderApplicationService.purchaseTicket(savedReservation.getId(), USER_ID))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("ist nicht reserviert");

        // Verify keine Order erstellt
        assertThat(orderRepository.findAll()).isEmpty();
    }

    // ==================== GET METHODS TESTS ====================

    @Test
    @DisplayName("getOrderById - Erfolg")
    void getOrderById_Success() {
        // Arrange - Erstelle Order in DB
        testSeat.hold(String.valueOf(testSeat.getId()), 15);
        testSeat = seatRepository.save(testSeat);

        Reservation testReservation = Reservation.createHold(testSeat.getId(), USER_ID, 15);
        testReservation = reservationRepository.save(testReservation);

        testSeat.updateHoldReservationId(String.valueOf(testReservation.getId()));
        testSeat = seatRepository.save(testSeat);

        Order createdOrder = orderApplicationService.purchaseTicket(testReservation.getId(), USER_ID);

        // Act
        Order result = orderApplicationService.getOrderById(createdOrder.getId());

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(createdOrder.getId());
        assertThat(result.getUserId()).isEqualTo(USER_ID);
    }

    @Test
    @DisplayName("getOrderById - Nicht gefunden")
    void getOrderById_NotFound() {
        // Act & Assert
        assertThatThrownBy(() -> orderApplicationService.getOrderById(999L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Order mit ID 999 nicht gefunden");
    }

    @Test
    @DisplayName("getOrdersByUserId - Erfolg")
    void getOrdersByUserId_Success() {
        // Arrange - Erstelle Order in DB
        testSeat.hold(String.valueOf(testSeat.getId()), 15);
        testSeat = seatRepository.save(testSeat);

        Reservation testReservation = Reservation.createHold(testSeat.getId(), USER_ID, 15);
        testReservation = reservationRepository.save(testReservation);

        testSeat.updateHoldReservationId(String.valueOf(testReservation.getId()));
        testSeat = seatRepository.save(testSeat);

        orderApplicationService.purchaseTicket(testReservation.getId(), USER_ID);

        // Act
        List<Order> result = orderApplicationService.getOrdersByUserId(USER_ID);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(USER_ID);
    }

    // ==================== HELPER METHODS ====================

    /**
     * Helper für Edge-Case-Testing: Setzt expiresAt via Reflection.
     * Nur für Test-Zwecke, da Reservation.expiresAt final ist.
     */
    private void setExpiresAt(Reservation reservation, LocalDateTime expiresAt) {
        try {
            Field field = Reservation.class.getDeclaredField("expiresAt");
            field.setAccessible(true);
            field.set(reservation, expiresAt);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set expiresAt via reflection", e);
        }
    }
}