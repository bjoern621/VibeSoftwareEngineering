package com.concertcomparison.application.service;

import com.concertcomparison.domain.model.Reservation;
import com.concertcomparison.domain.model.Seat;
import com.concertcomparison.domain.model.SeatStatus;
import com.concertcomparison.domain.repository.OrderRepository;
import com.concertcomparison.domain.repository.ReservationRepository;
import com.concertcomparison.domain.repository.SeatRepository;
import com.concertcomparison.presentation.dto.OrderResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * Concurrency Tests für Order-Purchase Flow.
 * 
 * <p><b>CRITICAL TEST:</b> Validiert, dass bei parallelen Kaufversuchen
 * auf denselben Hold maximal EINE Transaktion erfolgreich ist.</p>
 * 
 * <p>Verwendet ExecutorService mit 50+ Threads um Race Conditions zu simulieren.</p>
 */
@SpringBootTest
@DisplayName("Order Concurrency Tests")
class OrderConcurrencyTest {

    @Autowired
    private OrderApplicationService orderApplicationService;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private OrderRepository orderRepository;

    private static final Long CONCERT_ID = 999L;
    private static final String USER_ID = "concurrency-test-user";
    private static final Double PRICE = 99.99;

    @BeforeEach
    @Transactional
    void setUp() {
        // Cleanup für idempotente Tests
        orderRepository.findByUserId(USER_ID).forEach(order -> 
            orderRepository.delete(order));
        reservationRepository.findActiveByUserId(USER_ID).forEach(res -> 
            reservationRepository.delete(res));
    }

    /**
     * CRITICAL TEST: 50 parallele Purchase-Versuche auf denselben Hold.
     * 
     * <p>Erwartetes Verhalten:</p>
     * <ul>
     *   <li>Genau 1 erfolgreicher Kauf (200 OK)</li>
     *   <li>49 fehlgeschlagene Käufe (409 CONFLICT oder Exception)</li>
     *   <li>Seat-Status: SOLD (nicht HELD oder AVAILABLE)</li>
     *   <li>Genau 1 Order in Datenbank</li>
     * </ul>
     */
    @Test
    @DisplayName("50 parallele Purchase-Versuche sollten maximal 1 erfolgreichen Kauf erlauben")
    void concurrentPurchaseAttempts_ShouldAllowOnlyOneSale() throws InterruptedException, ExecutionException {
        // ========== ARRANGE ==========
        // 1. Seat erstellen und im Hold-Status sichern
        Seat seat = new Seat(CONCERT_ID, "CONCURRENCY-1", "VIP", "Block Test", "1", "1", PRICE);
        seat = seatRepository.save(seat);
        final Long seatId = seat.getId();

        // 2. Reservation erstellen
        Reservation reservation = Reservation.createHold(seatId, USER_ID, 60); // 60 min TTL
        reservation = reservationRepository.save(reservation);
        final Long reservationId = reservation.getId();

        // 3. Seat in HELD-Status setzen
        seat.hold(String.valueOf(reservationId), 60);
        seatRepository.save(seat);

        // Validierung: Seat ist wirklich HELD vor Test-Start
        Seat heldSeat = seatRepository.findById(seatId).orElseThrow();
        assertThat(heldSeat.getStatus()).isEqualTo(SeatStatus.HELD);

        // ========== ACT ==========
        // 50 parallele Purchase-Versuche
        final int THREAD_COUNT = 50;
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        
        // Thread-safe Zähler für Erfolge und Fehler
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);
        AtomicInteger otherErrorCount = new AtomicInteger(0);

        // Thread-safe Liste für Exceptions
        List<Exception> exceptions = Collections.synchronizedList(new ArrayList<>());

        // Submit 50 purchase tasks
        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < THREAD_COUNT; i++) {
            Future<?> future = executor.submit(() -> {
                try {
                    // Jeder Thread versucht, dasselbe Ticket zu kaufen
                    OrderResponseDTO result = orderApplicationService.purchaseTicket(reservationId, USER_ID);
                    
                    // Erfolg!
                    successCount.incrementAndGet();
                    
                } catch (IllegalStateException | IllegalArgumentException e) {
                    // Erwartete Business-Fehler (z.B. "Seat nicht HELD", "Reservation nicht aktiv")
                    conflictCount.incrementAndGet();
                    exceptions.add(e);
                    
                } catch (OptimisticLockingFailureException e) {
                    // Optimistic Locking Conflict (auch erwartet)
                    conflictCount.incrementAndGet();
                    exceptions.add(e);
                    
                } catch (Exception e) {
                    // Unerwartete Fehler
                    otherErrorCount.incrementAndGet();
                    exceptions.add(e);
                }
            });
            futures.add(future);
        }

        // Warte auf alle Threads
        for (Future<?> future : futures) {
            future.get(); // Blockiert bis Thread fertig, wirft ExecutionException bei Fehler
        }

        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);

        // ========== ASSERT ==========
        System.out.println("=== Concurrency Test Results ===");
        System.out.println("Erfolgreiche Käufe: " + successCount.get());
        System.out.println("Conflicts (409): " + conflictCount.get());
        System.out.println("Andere Fehler: " + otherErrorCount.get());
        System.out.println("Gesamt-Versuche: " + THREAD_COUNT);

        // 1. KRITISCH: Genau 1 erfolgreicher Kauf
        assertThat(successCount.get())
                .as("Genau 1 Thread sollte erfolgreich sein")
                .isEqualTo(1);

        // 2. KRITISCH: 49 fehlgeschlagene Käufe
        assertThat(conflictCount.get() + otherErrorCount.get())
                .as("49 Threads sollten fehlschlagen")
                .isEqualTo(THREAD_COUNT - 1);

        // 3. Keine unerwarteten Fehler
        assertThat(otherErrorCount.get())
                .as("Keine unerwarteten Fehler")
                .isEqualTo(0);

        // 4. Seat ist jetzt SOLD
        Seat finalSeat = seatRepository.findById(seatId).orElseThrow();
        assertThat(finalSeat.getStatus())
                .as("Seat sollte SOLD sein")
                .isEqualTo(SeatStatus.SOLD);
        assertThat(finalSeat.getHoldReservationId())
                .as("Hold-ReservationId sollte gelöscht sein")
                .isNull();

        // 5. Genau 1 Order in Datenbank
        long orderCount = orderRepository.countByUserId(USER_ID);
        assertThat(orderCount)
                .as("Genau 1 Order sollte erstellt worden sein")
                .isEqualTo(1);

        // Optional: Exceptions loggen für Debugging
        if (!exceptions.isEmpty()) {
            System.out.println("\n=== Caught Exceptions (erwartet) ===");
            exceptions.stream()
                    .map(e -> e.getClass().getSimpleName() + ": " + e.getMessage())
                    .distinct()
                    .forEach(System.out::println);
        }
    }

    /**
     * Test: Parallele Käufe von VERSCHIEDENEN Holds sollten alle erfolgreich sein.
     */
    @Test
    @DisplayName("10 parallele Käufe von verschiedenen Holds sollten alle erfolgreich sein")
    void concurrentPurchasesOfDifferentSeats_ShouldAllSucceed() throws InterruptedException, ExecutionException {
        // ========== ARRANGE ==========
        final int SEAT_COUNT = 10;
        List<Long> reservationIds = new ArrayList<>();

        // 10 verschiedene Seats mit Holds erstellen
        for (int i = 0; i < SEAT_COUNT; i++) {
            Seat seat = new Seat(CONCERT_ID, "SEAT-" + i, "VIP", "Block Test", String.valueOf(i), String.valueOf(i), PRICE);
            seat = seatRepository.save(seat);

            Reservation reservation = Reservation.createHold(seat.getId(), USER_ID, 60);
            reservation = reservationRepository.save(reservation);
            reservationIds.add(reservation.getId());

            seat.hold(String.valueOf(reservation.getId()), 60);
            seatRepository.save(seat);
        }

        // ========== ACT ==========
        ExecutorService executor = Executors.newFixedThreadPool(SEAT_COUNT);
        AtomicInteger successCount = new AtomicInteger(0);
        List<Future<?>> futures = new ArrayList<>();

        for (Long reservationId : reservationIds) {
            Future<?> future = executor.submit(() -> {
                try {
                    orderApplicationService.purchaseTicket(reservationId, USER_ID);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    System.err.println("Unexpected error: " + e.getMessage());
                }
            });
            futures.add(future);
        }

        for (Future<?> future : futures) {
            future.get();
        }

        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);

        // ========== ASSERT ==========
        assertThat(successCount.get())
                .as("Alle 10 Käufe sollten erfolgreich sein")
                .isEqualTo(SEAT_COUNT);

        long orderCount = orderRepository.countByUserId(USER_ID);
        assertThat(orderCount)
                .as("10 Orders sollten erstellt worden sein")
                .isEqualTo(SEAT_COUNT);
    }
}
