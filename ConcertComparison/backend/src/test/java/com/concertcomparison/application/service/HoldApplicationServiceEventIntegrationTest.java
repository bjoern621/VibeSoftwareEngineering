package com.concertcomparison.application.service;

import com.concertcomparison.domain.event.SeatStatusChangedEvent;
import com.concertcomparison.domain.model.Concert;
import com.concertcomparison.domain.model.Seat;
import com.concertcomparison.domain.model.SeatStatus;
import com.concertcomparison.domain.repository.ConcertRepository;
import com.concertcomparison.domain.repository.ReservationRepository;
import com.concertcomparison.domain.repository.SeatRepository;
import com.concertcomparison.presentation.dto.HoldResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationEventPublisher;
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
 * Integration Tests für HoldApplicationService - Event Publishing.
 * 
 * ECHTE DB TESTS - Keine Mocks!
 * Fokus: Verifizierung dass SeatStatusChangedEvents korrekt gepublisht werden.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("HoldApplicationService Event Publishing Integration Tests")
@Transactional
class HoldApplicationServiceEventIntegrationTest {

    @Autowired
    private HoldApplicationService holdApplicationService;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ConcertRepository concertRepository;

    @Autowired
    private TestEventListener testEventListener;

    private Concert testConcert;
    private Seat testSeat;
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

        // Erstelle Seat in DB
        testSeat = new Seat(
            testConcert.getId(),
            "A-1",
            "VIP",
            "Block A",
            "1",
            "1",
            99.0
        );
        testSeat = seatRepository.save(testSeat);
    }

    /**
     * US-05: Hold erstellen publisht SeatStatusChangedEvent (AVAILABLE → HELD).
     */
    @Test
    void createHold_shouldPublishHoldCreatedEvent() {
        // When: Hold erstellen
        HoldResponseDTO result = holdApplicationService.createHold(testSeat.getId(), USER_ID);

        // Then: Event wurde gepublisht
        await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
            List<SeatStatusChangedEvent> events = testEventListener.getEvents();
            assertThat(events).hasSize(1);

            SeatStatusChangedEvent event = events.get(0);
            assertThat(event.getSeatId()).isEqualTo(testSeat.getId());
            assertThat(event.getConcertId()).isEqualTo(testConcert.getId());
            assertThat(event.getOldStatus()).isEqualTo(SeatStatus.AVAILABLE);
            assertThat(event.getNewStatus()).isEqualTo(SeatStatus.HELD);
            assertThat(event.getUserId()).isEqualTo(USER_ID);
            assertThat(event.getReason()).isEqualTo("HOLD_CREATED");
        });
    }

    /**
     * US-05: Hold stornieren publisht SeatStatusChangedEvent (HELD → AVAILABLE).
     */
    @Test
    void releaseHold_shouldPublishHoldCancelledEvent() {
        // Given: Existierender Hold in DB
        HoldResponseDTO hold = holdApplicationService.createHold(testSeat.getId(), USER_ID);
        testEventListener.clear(); // Clear createHold event

        // When: Hold stornieren
        holdApplicationService.releaseHold(Long.parseLong(hold.holdId()));

        // Then: Event wurde gepublisht
        await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
            List<SeatStatusChangedEvent> events = testEventListener.getEvents();
            assertThat(events).hasSize(1);

            SeatStatusChangedEvent event = events.get(0);
            assertThat(event.getSeatId()).isEqualTo(testSeat.getId());
            assertThat(event.getConcertId()).isEqualTo(testConcert.getId());
            assertThat(event.getOldStatus()).isEqualTo(SeatStatus.HELD);
            assertThat(event.getNewStatus()).isEqualTo(SeatStatus.AVAILABLE);
            assertThat(event.getUserId()).isEqualTo(USER_ID);
            assertThat(event.getReason()).isEqualTo("HOLD_CANCELLED");
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
