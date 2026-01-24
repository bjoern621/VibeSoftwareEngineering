package com.concertcomparison.integration;

import com.concertcomparison.application.service.HoldApplicationService;
import com.concertcomparison.application.service.OrderApplicationService;
import com.concertcomparison.application.service.SeatApplicationService;
import com.concertcomparison.config.TestPaymentConfiguration;
import com.concertcomparison.domain.model.*;
import com.concertcomparison.domain.repository.ConcertRepository;
import com.concertcomparison.domain.repository.SeatRepository;
import com.concertcomparison.domain.repository.UserRepository;
import com.concertcomparison.presentation.dto.HoldResponseDTO;
import com.concertcomparison.presentation.dto.SeatAvailabilityResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration Test für Event-gesteuerte Cache-Invalidierung (US-05).
 * 
 * Testet den kompletten Flow:
 * 1. Client ruft Availability ab (wird gecacht)
 * 2. Seat-Änderung (Hold/Purchase) triggert Event
 * 3. EventListener invalidiert Cache
 * 4. Nächster Client-Poll liefert aktuelle Daten
 * 
 * Nutzt echten Spring Context mit Cache, Events, Repositories.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Import(TestPaymentConfiguration.class)
class SeatAvailabilityCacheEvictionIntegrationTest {

    @Autowired
    private HoldApplicationService holdApplicationService;

    @Autowired
    private OrderApplicationService orderApplicationService;

    @Autowired
    private SeatApplicationService seatApplicationService;

    @Autowired
    private ConcertRepository concertRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CacheManager cacheManager;

    private Concert concert;
    private Seat seat;
    private User user;

    @BeforeEach
    void setUp() {
        // Cache leeren
        Cache cache = cacheManager.getCache("seatAvailability");
        if (cache != null) {
            cache.clear();
        }

        // Test-Concert erstellen (Factory Method nutzen)
        concert = Concert.createConcert(
            "Integration Test Concert",
            LocalDateTime.now().plusDays(30),
            "Test Venue",
            "Test Description"
        );
        concert = concertRepository.save(concert);

        // Test-Seat erstellen (AVAILABLE)
        seat = new Seat(
            concert.getId(),
            "A-1",
            "VIP",
            "Block A",
            "1",
            "1",
            99.0
        );
        seat = seatRepository.save(seat);

        // Test-User erstellen (Factory Method nutzen)
        user = User.createUser(
            "test@example.com",
            "SecurePassword123!",
            "Test",
            "User",
            UserRole.USER
        );
        user = userRepository.save(user);
    }

    /**
     * US-05 Acceptance Criteria 1: Clients erhalten bei Seat-Änderungen aktuelle Daten.
     * 
     * Szenario:
     * 1. Initial: 1 AVAILABLE Seat
     * 2. Hold erstellen → Cache wird invalidiert
     * 3. Availability abrufen → 0 AVAILABLE, 1 HELD (aktuell)
     */
    @Test
    void whenHoldCreated_thenCacheInvalidated_andAvailabilityUpdated() throws InterruptedException {
        // GIVEN: Initial Availability abrufen (Prime Cache)
        SeatAvailabilityResponseDTO initialAvailability = 
            seatApplicationService.getSeatAvailability(concert.getId());
        
        assertThat(initialAvailability.getAvailabilityByCategory()).hasSize(1);
        assertThat(initialAvailability.getAvailabilityByCategory().get(0).getAvailable()).isEqualTo(1);
        assertThat(initialAvailability.getAvailabilityByCategory().get(0).getHeld()).isEqualTo(0);

        // Verify Cache ist befüllt
        Cache cache = cacheManager.getCache("seatAvailability");
        assertThat(cache).isNotNull();
        assertThat(cache.get(concert.getId())).isNotNull();

        // WHEN: Hold erstellen (Event wird gepublisht, Cache invalidiert)
        holdApplicationService.createHold(seat.getId(), user.getId().toString());
        
        // Event-Processing ist asynchron (@Async) - kurz warten
        Thread.sleep(100);

        // THEN: Cache wurde invalidiert
        assertThat(cache.get(concert.getId())).isNull();

        // THEN: Neue Availability-Abfrage liefert aktuelle Daten (0 AVAILABLE, 1 HELD)
        SeatAvailabilityResponseDTO updatedAvailability = 
            seatApplicationService.getSeatAvailability(concert.getId());
        
        assertThat(updatedAvailability.getAvailabilityByCategory()).hasSize(1);
        assertThat(updatedAvailability.getAvailabilityByCategory().get(0).getAvailable()).isEqualTo(0);
        assertThat(updatedAvailability.getAvailabilityByCategory().get(0).getHeld()).isEqualTo(1);
    }

    /**
     * US-05 Acceptance Criteria 2: Keine inkonsistenten Werte.
     * 
     * Szenario:
     * 1. Hold erstellen
     * 2. Ticket kaufen → Cache wird invalidiert
     * 3. Availability zeigt 1 SOLD (kein HELD mehr)
     */
    @Test
    void whenTicketPurchased_thenCacheInvalidated_andNoInconsistentValues() throws InterruptedException {
        // GIVEN: Hold erstellen
        HoldResponseDTO hold = holdApplicationService.createHold(seat.getId(), user.getId().toString());
        Thread.sleep(100); // Event-Processing
        
        // Cache primen
        SeatAvailabilityResponseDTO afterHold = 
            seatApplicationService.getSeatAvailability(concert.getId());
        assertThat(afterHold.getAvailabilityByCategory().get(0).getHeld()).isEqualTo(1);

        // WHEN: Ticket kaufen
        Order order = orderApplicationService.purchaseTicket(
            Long.valueOf(hold.holdId()), 
            user.getId().toString(),
            PaymentMethod.CREDIT_CARD
        );
        Thread.sleep(100); // Event-Processing

        // THEN: Cache invalidiert
        Cache cache = cacheManager.getCache("seatAvailability");
        assertThat(cache).isNotNull();
        assertThat(cache.get(concert.getId())).isNull();

        // THEN: Availability zeigt SOLD, kein HELD mehr (konsistent)
        SeatAvailabilityResponseDTO afterPurchase = 
            seatApplicationService.getSeatAvailability(concert.getId());
        
        assertThat(afterPurchase.getAvailabilityByCategory()).hasSize(1);
        assertThat(afterPurchase.getAvailabilityByCategory().get(0).getAvailable()).isEqualTo(0);
        assertThat(afterPurchase.getAvailabilityByCategory().get(0).getHeld()).isEqualTo(0);
        assertThat(afterPurchase.getAvailabilityByCategory().get(0).getSold()).isEqualTo(1);
    }

    /**
     * US-05 Acceptance Criteria 3: System bleibt stabil bei Lastspitzen.
     * 
     * Simuliert 10 schnelle Hold-Operationen.
     * Cache muss bei jedem Event invalidiert werden.
     */
    @Test
    void whenMultipleHoldsCreated_thenSystemStableAndCacheConsistent() throws InterruptedException {
        // GIVEN: 10 zusätzliche Seats
        for (int i = 2; i <= 11; i++) {
            Seat s = new Seat(concert.getId(), "A-" + i, "VIP", "Block A", "1", String.valueOf(i), 99.0);
            seatRepository.save(s);
        }

        // Initial Availability (Prime Cache)
        SeatAvailabilityResponseDTO initial = 
            seatApplicationService.getSeatAvailability(concert.getId());
        assertThat(initial.getAvailabilityByCategory().get(0).getAvailable()).isEqualTo(11);

        // WHEN: 10 Holds schnell hintereinander erstellen
        var allSeats = seatRepository.findByConcertId(concert.getId());
        for (int i = 0; i < 10; i++) {
            Seat s = allSeats.get(i);
            holdApplicationService.createHold(s.getId(), user.getId().toString());
        }

        // Event-Processing abwarten (asynchron)
        Thread.sleep(500);

        // THEN: Availability korrekt (1 AVAILABLE, 10 HELD)
        SeatAvailabilityResponseDTO afterHolds = 
            seatApplicationService.getSeatAvailability(concert.getId());
        
        assertThat(afterHolds.getAvailabilityByCategory().get(0).getAvailable()).isEqualTo(1);
        assertThat(afterHolds.getAvailabilityByCategory().get(0).getHeld()).isEqualTo(10);
        assertThat(afterHolds.getAvailabilityByCategory().get(0).getSold()).isEqualTo(0);
    }

    /**
     * US-05: Hold-Stornierung invalidiert Cache.
     */
    @Test
    void whenHoldCancelled_thenCacheInvalidatedAndSeatAvailableAgain() throws InterruptedException {
        // GIVEN: Hold erstellen
        HoldResponseDTO hold = holdApplicationService.createHold(seat.getId(), user.getId().toString());
        Thread.sleep(100);
        
        SeatAvailabilityResponseDTO afterHold = 
            seatApplicationService.getSeatAvailability(concert.getId());
        assertThat(afterHold.getAvailabilityByCategory().get(0).getHeld()).isEqualTo(1);

        // WHEN: Hold stornieren
        holdApplicationService.releaseHold(Long.valueOf(hold.holdId()));
        Thread.sleep(100);

        // THEN: Cache invalidiert
        Cache cache = cacheManager.getCache("seatAvailability");
        assertThat(cache).isNotNull();
        assertThat(cache.get(concert.getId())).isNull();

        // THEN: Seat wieder AVAILABLE
        SeatAvailabilityResponseDTO afterCancel = 
            seatApplicationService.getSeatAvailability(concert.getId());
        
        assertThat(afterCancel.getAvailabilityByCategory().get(0).getAvailable()).isEqualTo(1);
        assertThat(afterCancel.getAvailabilityByCategory().get(0).getHeld()).isEqualTo(0);
    }
}
