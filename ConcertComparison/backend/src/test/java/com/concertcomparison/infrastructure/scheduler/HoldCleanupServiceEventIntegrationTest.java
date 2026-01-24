package com.concertcomparison.infrastructure.scheduler;

import com.concertcomparison.application.service.HoldApplicationService;
import com.concertcomparison.domain.event.SeatStatusChangedEvent;
import com.concertcomparison.domain.model.Concert;
import com.concertcomparison.domain.model.Seat;
import com.concertcomparison.domain.model.SeatStatus;
import com.concertcomparison.domain.repository.ConcertRepository;
import com.concertcomparison.domain.repository.ReservationRepository;
import com.concertcomparison.domain.repository.SeatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.*;
import java.util.concurrent.TimeUnit;

/**
 * Integration Tests für HoldCleanupService - Event Publishing.
 * 
 * ECHTE DB TESTS - Keine Mocks!
 * Fokus: Verifizierung dass SeatStatusChangedEvents gepublisht werden bei Auto-Cleanup.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("HoldCleanupService Event Publishing Integration Tests")
@Transactional
class HoldCleanupServiceEventIntegrationTest {

    @Autowired
    private HoldCleanupService holdCleanupService;

    @Autowired
    private HoldApplicationService holdApplicationService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private ConcertRepository concertRepository;

    @Autowired
    private TestEventListener testEventListener;

    private Concert testConcert;
    private Seat testSeat1;
    private Seat testSeat2;
    private Seat testSeat3;
    private static final String USER_ID = "test-user@example.com";

    @BeforeEach
    void setUp() {
        // Clean up
        reservationRepository.deleteAll();
        seatRepository.deleteAll();
        concertRepository.deleteAll();
        testEventListener.clear();

        // Erstelle Concert in DB
        testConcert = Concert.createConcert(
            "Rock Festival",
            LocalDateTime.now().plusDays(30),
            "Olympiastadion Berlin",
            "Beste Rock Bands"
        );
        testConcert = concertRepository.save(testConcert);

        // Erstelle Seats in DB
        testSeat1 = new Seat(testConcert.getId(), "A-1", "VIP", "Block A", "1", "1", 99.0);
        testSeat1 = seatRepository.save(testSeat1);

        testSeat2 = new Seat(testConcert.getId(), "A-2", "VIP", "Block A", "1", "2", 99.0);
        testSeat2 = seatRepository.save(testSeat2);

        testSeat3 = new Seat(testConcert.getId(), "B-1", "STANDARD", "Block B", "1", "1", 49.0);
        testSeat3 = seatRepository.save(testSeat3);
    }

    /**
     * US-05: Cleanup abgelaufener Hold publisht SeatStatusChangedEvent (HELD → AVAILABLE).
     * 
     * Nutzt DB-Manipulation um Hold künstlich abzulaufen.
     */
    @Test
    void cleanupExpiredHolds_shouldPublishHoldExpiredEvent() {
        // Given: Einen Hold erstellen
        var hold = holdApplicationService.createHold(testSeat1.getId(), USER_ID);
        testEventListener.clear(); // Clear createHold event

        // Manipuliere DB: Setze expiresAt in die Vergangenheit
        reservationRepository.findById(Long.parseLong(hold.holdId())).ifPresent(reservation -> {
            reservation.expireNow(); // Utility-Methode oder direktes setzen
            reservationRepository.save(reservation);
        });

        // When: Cleanup durchführen
        int cleaned = holdCleanupService.cleanupExpiredHolds();

        // Then: Event wurde gepublisht
        assertThat(cleaned).isEqualTo(1);

        await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
            List<SeatStatusChangedEvent> events = testEventListener.getEvents();
            assertThat(events).hasSize(1);

            SeatStatusChangedEvent event = events.get(0);
            assertThat(event.getSeatId()).isEqualTo(testSeat1.getId());
            assertThat(event.getConcertId()).isEqualTo(testConcert.getId());
            assertThat(event.getOldStatus()).isEqualTo(SeatStatus.HELD);
            assertThat(event.getNewStatus()).isEqualTo(SeatStatus.AVAILABLE);
            assertThat(event.getUserId()).isNull(); // System-Action
            assertThat(event.getReason()).isEqualTo("HOLD_EXPIRED");
        });
    }

    /**
     * US-05: Cleanup mit mehreren abgelaufenen Holds publisht mehrere Events.
     */
    @Test
    void cleanupExpiredHolds_withMultipleExpired_shouldPublishMultipleEvents() {
        // Given: 3 Holds erstellen
        var hold1 = holdApplicationService.createHold(testSeat1.getId(), USER_ID);
        var hold2 = holdApplicationService.createHold(testSeat2.getId(), USER_ID);
        var hold3 = holdApplicationService.createHold(testSeat3.getId(), USER_ID);
        testEventListener.clear(); // Clear createHold events

        // Alle Holds ablaufen lassen
        reservationRepository.findById(Long.parseLong(hold1.holdId())).ifPresent(r -> {
            r.expireNow();
            reservationRepository.save(r);
        });
        reservationRepository.findById(Long.parseLong(hold2.holdId())).ifPresent(r -> {
            r.expireNow();
            reservationRepository.save(r);
        });
        reservationRepository.findById(Long.parseLong(hold3.holdId())).ifPresent(r -> {
            r.expireNow();
            reservationRepository.save(r);
        });

        // When: Cleanup
        int cleaned = holdCleanupService.cleanupExpiredHolds();

        // Then: 3 Events gepublisht
        assertThat(cleaned).isEqualTo(3);

        await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
            List<SeatStatusChangedEvent> events = testEventListener.getEvents();
            assertThat(events).hasSize(3);

            // Verify alle Events sind HOLD_EXPIRED
            assertThat(events)
                .allMatch(e -> e.getReason().equals("HOLD_EXPIRED"))
                .allMatch(e -> e.getOldStatus() == SeatStatus.HELD)
                .allMatch(e -> e.getNewStatus() == SeatStatus.AVAILABLE);
        });
    }

    /**
     * US-05: Cleanup ignoriert bereits freigegebene Holds.
     */
    @Test
    void cleanupExpiredHolds_whenNoExpiredHolds_shouldNotPublishEvents() {
        // Given: Einen aktiven Hold (nicht abgelaufen)
        holdApplicationService.createHold(testSeat1.getId(), USER_ID);
        testEventListener.clear();

        // When: Cleanup (findet nichts)
        int cleaned = holdCleanupService.cleanupExpiredHolds();

        // Then: Keine Events
        assertThat(cleaned).isEqualTo(0);

        await().pollDelay(500, TimeUnit.MILLISECONDS)
               .atMost(1, TimeUnit.SECONDS)
               .untilAsserted(() -> {
                   assertThat(testEventListener.getEvents()).isEmpty();
               });
    }

    /**
     * Test Configuration: Event Listener zum Sammeln von Events.
     */
    @TestConfiguration
    static class TestEventListenerConfig {
        
        @Bean
        public TestEventListener testEventListener() {
            return new TestEventListener();
        }
    }

    /**
     * Test Event Listener - sammelt alle SeatStatusChangedEvents.
     */
    static class TestEventListener {
        
        private final List<SeatStatusChangedEvent> events = new ArrayList<>();

        @EventListener
        public void handleSeatStatusChanged(SeatStatusChangedEvent event) {
            events.add(event);
        }

        public List<SeatStatusChangedEvent> getEvents() {
            return new ArrayList<>(events);
        }

        public void clear() {
            events.clear();
        }
    }
}
