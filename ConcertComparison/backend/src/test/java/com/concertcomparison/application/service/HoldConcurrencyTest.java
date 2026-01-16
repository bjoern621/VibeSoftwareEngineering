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
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Concurrency Tests für Hold-Operationen.
 * 
 * Testet Race Conditions, Optimistic Locking und parallele Zugriffe.
 * Diese Tests simulieren echte High-Traffic Szenarien mit 50-100+ gleichzeitigen Requests.
 * 
 * WICHTIG: Diese Tests sind INTEGRATION Tests und benötigen echte DB-Transaktionen!
 */
@SpringBootTest
@ActiveProfiles("test")
class HoldConcurrencyTest {

    @Autowired
    private HoldApplicationService holdApplicationService;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    private Long testSeatId;

    /**
     * Setup für Concurrency Tests.
     * 
     * WICHTIG: KEIN @Transactional hier!
     * Concurrency Tests benötigen committed data, damit parallele Threads
     * die Testdaten sehen können. Mit @Transactional würde Rollback erfolgen.
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
        // Testdaten: Ein einzelner Seat für Race Condition Tests
        // Wird automatisch committed (kein @Transactional!)
        Seat seat = new Seat(1L, "TEST-A-1", "VIP", "Block A", "1", "1", 99.99);
        seat = seatRepository.save(seat);
        testSeatId = seat.getId();
    }

    @Test
    @DisplayName("RACE CONDITION: Nur 1 von 100 parallelen Requests darf Seat reservieren")
    void shouldPreventDoubleSale_When100UsersCompeteForSameSeat() throws InterruptedException {
        // Arrange: 100 konkurrierende Threads
        int threadCount = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);
        List<String> successfulUsers = new CopyOnWriteArrayList<>();

        // Act: Alle 100 Threads versuchen gleichzeitig zu reservieren
        for (int i = 0; i < threadCount; i++) {
            final String userId = "user-" + i;
            executor.submit(() -> {
                try {
                    // Warte bis alle Threads bereit sind
                    startLatch.await();

                    // Versuche Hold zu erstellen
                    HoldResponseDTO result = holdApplicationService.createHold(testSeatId, userId);
                    successCount.incrementAndGet();
                    successfulUsers.add(userId);

                } catch (ObjectOptimisticLockingFailureException e) {
                    // Erwarteter Concurrency Conflict
                    conflictCount.incrementAndGet();
                } catch (IllegalStateException e) {
                    // Seat already held
                    conflictCount.incrementAndGet();
                } catch (Exception e) {
                    // Unerwarteter Fehler
                    System.err.println("Unexpected error for " + userId + ": " + e.getMessage());
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        // Start alle Threads gleichzeitig
        startLatch.countDown();

        // Warte bis alle fertig sind (max 10 Sekunden)
        boolean completed = doneLatch.await(10, TimeUnit.SECONDS);

        executor.shutdown();

        // Assert: Genau 1 Erfolg, 99 Konflikte
        assertThat(completed).isTrue();
        assertThat(successCount.get()).isEqualTo(1)
            .withFailMessage("Genau 1 User sollte erfolgreich sein, aber waren: %d", successCount.get());
        assertThat(conflictCount.get()).isEqualTo(99)
            .withFailMessage("99 Conflicts erwartet, aber waren: %d", conflictCount.get());
        assertThat(successfulUsers).hasSize(1);

        // Verify: Seat ist HELD in DB
        Seat seat = seatRepository.findById(testSeatId).orElseThrow();
        assertThat(seat.getStatus()).isEqualTo(SeatStatus.HELD);

        // Verify: Genau 1 Reservation existiert
        List<Reservation> reservations = reservationRepository.findAll();
        assertThat(reservations).hasSize(1);
    }

    @Test
    @DisplayName("OPTIMISTIC LOCKING: Version-Increment verhindert Lost Updates")
    void shouldIncrementVersion_WhenSeatIsModified() throws InterruptedException, ExecutionException, TimeoutException {
        // Arrange: 2 Threads laden denselben Seat
        CountDownLatch loadLatch = new CountDownLatch(2);
        CountDownLatch modifyLatch = new CountDownLatch(1);

        final Long[] initialVersion = new Long[1];
        final Long[] finalVersion = new Long[1];

        ExecutorService executor = Executors.newFixedThreadPool(2);

        // Thread 1: Lädt, wartet, modifiziert
        Future<Boolean> thread1 = executor.submit(() -> {
            Seat seat = seatRepository.findById(testSeatId).orElseThrow();
            initialVersion[0] = seat.getVersion();
            loadLatch.countDown();
            modifyLatch.await(); // Warte bis Thread 2 auch geladen hat

            seat.hold("user-1", 15);
            seatRepository.save(seat);
            return true;
        });

        // Thread 2: Lädt, wartet, versucht zu modifizieren (sollte fehlschlagen)
        Future<Boolean> thread2 = executor.submit(() -> {
            Seat seat = seatRepository.findById(testSeatId).orElseThrow();
            loadLatch.countDown();
            loadLatch.await(); // Warte bis beide geladen haben
            modifyLatch.countDown(); // Signal an Thread 1

            Thread.sleep(100); // Thread 1 Zeit geben zu committen

            try {
                seat.hold("user-2", 15);
                seatRepository.save(seat);
                return false; // Sollte nicht erreicht werden!
            } catch (ObjectOptimisticLockingFailureException e) {
                return true; // Erwarteter Conflict
            }
        });

        // Assert
        assertThat(thread1.get(5, TimeUnit.SECONDS)).isTrue();
        assertThat(thread2.get(5, TimeUnit.SECONDS)).isTrue();

        executor.shutdown();

        // Verify: Version wurde inkrementiert
        Seat finalSeat = seatRepository.findById(testSeatId).orElseThrow();
        finalVersion[0] = finalSeat.getVersion();
        assertThat(finalVersion[0]).isGreaterThan(initialVersion[0]);
        assertThat(finalSeat.getStatus()).isEqualTo(SeatStatus.HELD);
    }

    @Test
    @DisplayName("HIGH LOAD: 50 parallele Holds auf unterschiedliche Seats sollten alle erfolgreich sein")
    void shouldHandle50ParallelHolds_OnDifferentSeats() throws InterruptedException {
        // Arrange: 50 verschiedene Seats erstellen
        int seatCount = 50;
        List<Long> seatIds = new ArrayList<>();

        for (int i = 0; i < seatCount; i++) {
            Seat seat = new Seat(1L, "SEAT-" + i, "CATEGORY_A", "Block B", "1", String.valueOf(i), 79.99);
            seat = seatRepository.save(seat);
            seatIds.add(seat.getId());
        }

        // Act: 50 parallele Holds
        ExecutorService executor = Executors.newFixedThreadPool(seatCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(seatCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < seatCount; i++) {
            final Long seatId = seatIds.get(i);
            final String userId = "user-" + i;

            executor.submit(() -> {
                try {
                    startLatch.await();
                    holdApplicationService.createHold(seatId, userId);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    System.err.println("Failed for seat " + seatId + ": " + e.getMessage());
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        boolean completed = doneLatch.await(15, TimeUnit.SECONDS);
        executor.shutdown();

        // Assert: Alle 50 sollten erfolgreich sein (keine Konflikte bei verschiedenen Seats)
        assertThat(completed).isTrue();
        assertThat(successCount.get()).isEqualTo(seatCount);
        assertThat(failCount.get()).isZero();

        // Verify: 50 Reservations in DB
        List<Reservation> reservations = reservationRepository.findAll();
        assertThat(reservations).hasSize(seatCount);
    }

    @Test
    @DisplayName("CLEANUP: Sollte abgelaufene Holds freigeben ohne Race Conditions")
    void shouldHandleConcurrentCleanup_WithoutDataCorruption() throws InterruptedException {
        // Arrange: Mehrere abgelaufene Holds
        for (int i = 0; i < 10; i++) {
            Seat seat = new Seat(1L, "CLEANUP-" + i, "VIP", "Block C", "2", String.valueOf(i), 129.99);
            seat = seatRepository.save(seat);
            seat.hold("expired-" + i, java.time.LocalDateTime.now().minusMinutes(20));
            seatRepository.save(seat);

            Reservation res = Reservation.createHold(seat.getId(), "user-" + i, 15);
            reservationRepository.save(res);
            res.expire();
            reservationRepository.save(res);
        }

        // Act: 5 parallele Cleanup-Threads
        ExecutorService executor = Executors.newFixedThreadPool(5);
        CountDownLatch latch = new CountDownLatch(5);

        for (int i = 0; i < 5; i++) {
            executor.submit(() -> {
                try {
                    // Simuliere Cleanup (würde eigentlich HoldCleanupService aufrufen)
                    List<Seat> expiredSeats = seatRepository.findAll().stream()
                        .filter(s -> s.getStatus() == SeatStatus.HELD)
                        .filter(s -> s.getHoldExpiresAt() != null)
                        .filter(s -> s.getHoldExpiresAt().isBefore(java.time.LocalDateTime.now()))
                        .toList();

                    for (Seat seat : expiredSeats) {
                        try {
                            seat.releaseHold();
                            seatRepository.save(seat);
                        } catch (ObjectOptimisticLockingFailureException e) {
                            // Expected conflict, retry handled by scheduler
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        boolean completed = latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // Assert: Kein Crash, alle Seats wurden released (evtl. mit Retries)
        assertThat(completed).isTrue();

        // Gib Zeit für finale Commits
        Thread.sleep(500);

        List<Seat> heldSeats = seatRepository.findAll().stream()
            .filter(s -> s.getStatus() == SeatStatus.HELD)
            .toList();

        // Alle sollten freigegeben sein
        assertThat(heldSeats).isEmpty();
    }
}
