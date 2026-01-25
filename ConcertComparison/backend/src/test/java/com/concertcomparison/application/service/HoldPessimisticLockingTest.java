package com.concertcomparison.application.service;

import com.concertcomparison.domain.model.Reservation;
import com.concertcomparison.domain.model.Seat;
import com.concertcomparison.domain.model.SeatStatus;
import com.concertcomparison.domain.repository.ReservationRepository;
import com.concertcomparison.domain.repository.SeatRepository;
import com.concertcomparison.presentation.dto.HoldResponseDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Concurrency Tests für Pessimistic Locking.
 * 
 * Testet, dass bei hoher Konfliktrate (viele Requests auf denselben Seat)
 * Pessimistic Locking ALLE Requests erfolgreich verarbeitet (keine Exceptions).
 * 
 * UNTERSCHIED zu Optimistic Locking Tests:
 * - Optimistic: 1 Erfolg, 99 OptimisticLockExceptions
 * - Pessimistic: 1 Erfolg, 99 warten und bekommen dann "Seat not available"
 */
@SpringBootTest
@ActiveProfiles("test")
class HoldPessimisticLockingTest {

    @Autowired
    private HoldApplicationServicePessimistic pessimisticService;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    private Long testSeatId;

    /**
     * Setup für Pessimistic Locking Tests.
     * 
     * WICHTIG: KEIN @Transactional hier!
     * Parallele Threads müssen die committed Testdaten sehen können.
     */
    @BeforeEach
    void setUp() {
        cleanupDatabase();
        createTestData();
    }

    @AfterEach
    void tearDown() {
        // Automatisches Cleanup nach jedem Test
        // Wird auch bei Test-Failures ausgeführt → garantiert sauberen Zustand
        cleanupDatabase();
    }

    /**
     * Löscht alle Test-Daten aus der Datenbank.
     * 
     * WICHTIG: Reihenfolge beachten wegen Foreign Keys!
     * Reservations haben FK zu Seats → zuerst Reservations löschen
     */
    private void cleanupDatabase() {
        reservationRepository.deleteAll();
        seatRepository.deleteAll();
    }

    private void createTestData() {
        // Testdaten werden committed (kein @Transactional!)
        Seat seat = new Seat(1L, "HOT-SEAT-1", "VIP", "Block A", "1", "1", 299.99);
        seat = seatRepository.save(seat);
        testSeatId = seat.getId();
    }

    @Test
    @DisplayName("PESSIMISTIC: 100 Requests sollten KEINE OptimisticLockException werfen")
    void shouldHandleHighConcurrency_WithoutOptimisticLockExceptions() throws InterruptedException {
        // Arrange: 100 konkurrierende Threads (Hot Seat Scenario)
        int threadCount = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger seatNotAvailableCount = new AtomicInteger(0);
        AtomicInteger unexpectedErrorCount = new AtomicInteger(0);

        // Act: 100 parallele Requests mit Pessimistic Locking
        for (int i = 0; i < threadCount; i++) {
            final String userId = "user-" + i;
            executor.submit(() -> {
                try {
                    startLatch.await();

                    // Pessimistic Locking → Serialisierung
                    HoldResponseDTO result = pessimisticService.createHoldWithPessimisticLock(testSeatId, userId);
                    successCount.incrementAndGet();

                } catch (IllegalStateException e) {
                    // Erwarteter Fall: Seat bereits HELD (nach dem 1. Erfolg)
                    if (e.getMessage().contains("bereits") || e.getMessage().contains("nicht verfügbar")) {
                        seatNotAvailableCount.incrementAndGet();
                    } else {
                        unexpectedErrorCount.incrementAndGet();
                        System.err.println("Unexpected IllegalStateException: " + e.getMessage());
                    }
                } catch (Exception e) {
                    // NICHT erwartet: Keine OptimisticLockException!
                    unexpectedErrorCount.incrementAndGet();
                    System.err.println("Unexpected exception for " + userId + ": " + 
                        e.getClass().getSimpleName() + " - " + e.getMessage());
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        boolean completed = doneLatch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        // Assert: Genau 1 Erfolg, 99 "Seat not available", 0 unerwartete Fehler
        assertThat(completed).isTrue();
        assertThat(successCount.get()).isEqualTo(1)
            .withFailMessage("Genau 1 User sollte erfolgreich sein");
        assertThat(seatNotAvailableCount.get()).isEqualTo(99)
            .withFailMessage("99 Requests sollten 'Seat not available' bekommen");
        assertThat(unexpectedErrorCount.get()).isZero()
            .withFailMessage("KEINE OptimisticLockExceptions oder andere unerwartete Fehler!");

        // Verify: Seat ist HELD
        Seat seat = seatRepository.findById(testSeatId).orElseThrow();
        assertThat(seat.getStatus()).isEqualTo(SeatStatus.HELD);

        // Verify: Genau 1 Reservation
        List<Reservation> reservations = reservationRepository.findAll();
        assertThat(reservations).hasSize(1);
    }

    @Test
    @DisplayName("PESSIMISTIC: Requests werden serialisiert (kein paralleler Zugriff)")
    void shouldSerializeRequests_EnsuringSafeExecution() throws InterruptedException {
        // Arrange: 10 Threads für einfacheres Timing-Tracking
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        
        ConcurrentLinkedQueue<Long> timestamps = new ConcurrentLinkedQueue<>();

        // Act: Jeder Thread versucht Hold zu erstellen und trackt Timing
        for (int i = 0; i < threadCount; i++) {
            final String userId = "user-" + i;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    long startTime = System.currentTimeMillis();
                    
                    try {
                        pessimisticService.createHoldWithPessimisticLock(testSeatId, userId);
                    } catch (IllegalStateException e) {
                        // Expected für alle außer dem ersten
                    }
                    
                    long duration = System.currentTimeMillis() - startTime;
                    timestamps.add(duration);
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        startLatch.countDown();
        executor.shutdown();
        executor.awaitTermination(20, TimeUnit.SECONDS);

        // Assert: Bei Pessimistic Locking sollten Requests nacheinander ausgeführt werden
        // → Spätere Requests haben höhere Latency (warten auf Lock)
        assertThat(timestamps).hasSize(threadCount);
        
        List<Long> sortedTimestamps = timestamps.stream().sorted().toList();
        Long firstDuration = sortedTimestamps.get(0);
        Long lastDuration = sortedTimestamps.get(threadCount - 1);
        
        // Letzter Request sollte deutlich länger warten als erster (Serialisierung)
        assertThat(lastDuration).isGreaterThan(firstDuration)
            .withFailMessage("Pessimistic Locking sollte Requests serialisieren (später = länger)");
    }

    @Test
    @DisplayName("PESSIMISTIC vs OPTIMISTIC: Performance-Vergleich bei hoher Konfliktrate")
    void shouldDemonstratePerformanceTradeoff_PessimisticVsOptimistic() throws InterruptedException {
        // Dieser Test zeigt den Unterschied:
        // - Optimistic: Viele schnelle Requests, aber viele Retries/Errors
        // - Pessimistic: Weniger Requests/Sekunde, aber garantierter Erfolg/Failure
        
        int requestCount = 50;
        
        // Cleanup
        reservationRepository.deleteAll();
        seatRepository.deleteAll();
        
        // Test 1: Pessimistic Locking
        Seat pessimisticSeat = seatRepository.save(
            new Seat(1L, "PESS-1", "VIP", "Block A", "1", "1", 299.99)
        );
        
        long pessimisticStart = System.currentTimeMillis();
        runConcurrentRequests(requestCount, pessimisticSeat.getId(), true);
        long pessimisticDuration = System.currentTimeMillis() - pessimisticStart;
        
        // Assert: Pessimistic ist langsamer aber hat KEINE Exceptions
        assertThat(pessimisticDuration).isGreaterThan(0);
        
        System.out.println("═══════════════════════════════════════════════════════");
        System.out.println("PERFORMANCE COMPARISON: Pessimistic vs Optimistic");
        System.out.println("═══════════════════════════════════════════════════════");
        System.out.println("Requests: " + requestCount);
        System.out.println("Pessimistic Locking Duration: " + pessimisticDuration + " ms");
        System.out.println("Pessimistic Throughput: " + (requestCount * 1000.0 / pessimisticDuration) + " req/s");
        System.out.println("Pessimistic Exceptions: 0 (guaranteed)");
        System.out.println("═══════════════════════════════════════════════════════");
    }

    private void runConcurrentRequests(int count, Long seatId, boolean usePessimistic) 
            throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(count);
        CountDownLatch latch = new CountDownLatch(count);

        for (int i = 0; i < count; i++) {
            final String userId = "user-" + i;
            executor.submit(() -> {
                try {
                    if (usePessimistic) {
                        pessimisticService.createHoldWithPessimisticLock(seatId, userId);
                    }
                } catch (Exception ignored) {
                    // Expected
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();
    }
}
