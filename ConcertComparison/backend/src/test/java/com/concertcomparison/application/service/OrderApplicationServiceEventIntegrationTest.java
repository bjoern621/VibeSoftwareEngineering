package com.concertcomparison.application.service;

import com.concertcomparison.domain.event.SeatStatusChangedEvent;
import com.concertcomparison.domain.model.*;
import com.concertcomparison.domain.repository.ConcertRepository;
import com.concertcomparison.domain.repository.OrderRepository;
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
 * Integration Tests für OrderApplicationService - Event Publishing.
 * 
 * ECHTE DB TESTS - Keine Mocks!
 * Fokus: Verifizierung dass SeatStatusChangedEvents korrekt gepublisht werden.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("OrderApplicationService Event Publishing Integration Tests")
@Transactional
class OrderApplicationServiceEventIntegrationTest {

    @Autowired
    private OrderApplicationService orderApplicationService;

    @Autowired
    private HoldApplicationService holdApplicationService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private SeatRepository seatRepository;

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
        orderRepository.deleteAll();
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
     * US-05: Ticket kaufen publisht SeatStatusChangedEvent (HELD → SOLD).
     */
    @Test
    void purchaseTicket_shouldPublishTicketPurchasedEvent() {
        // Given: Existierender Hold in DB
        var hold = holdApplicationService.createHold(testSeat.getId(), USER_ID);
        testEventListener.clear(); // Clear createHold event

        // When: Ticket kaufen
        Order order = orderApplicationService.purchaseTicket(Long.parseLong(hold.holdId()), USER_ID, PaymentMethod.CREDIT_CARD);

        // Then: Event wurde gepublisht
        await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
            List<SeatStatusChangedEvent> events = testEventListener.getEvents();
            assertThat(events).hasSize(1);

            SeatStatusChangedEvent event = events.get(0);
            assertThat(event.getSeatId()).isEqualTo(testSeat.getId());
            assertThat(event.getConcertId()).isEqualTo(testConcert.getId());
            assertThat(event.getOldStatus()).isEqualTo(SeatStatus.HELD);
            assertThat(event.getNewStatus()).isEqualTo(SeatStatus.SOLD);
            assertThat(event.getUserId()).isEqualTo(USER_ID);
            assertThat(event.getReason()).isEqualTo("TICKET_PURCHASED");
        });

        // Verify Order wurde erstellt
        assertThat(order).isNotNull();
        assertThat(order.getSeatId()).isEqualTo(testSeat.getId());
    }

    /**
     * US-05: Bei fehlgeschlagenem Purchase wird KEIN Event gepublisht (Transaction Rollback).
     */
    @Test
    void purchaseTicket_whenReservationNotFound_shouldNotPublishEvent() {
        // When/Then: Exception und KEIN Event
        Long nonExistentHoldId = 99999L;
        
        assertThatThrownBy(() -> orderApplicationService.purchaseTicket(nonExistentHoldId, USER_ID, PaymentMethod.CREDIT_CARD))
            .isInstanceOf(com.concertcomparison.domain.exception.ReservationNotFoundException.class);

        // Verify: Keine Events gepublisht
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
