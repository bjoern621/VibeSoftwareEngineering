package com.concertcomparison.infrastructure.config;

import com.concertcomparison.domain.model.Order;
import com.concertcomparison.domain.model.Reservation;
import com.concertcomparison.domain.model.Seat;
import com.concertcomparison.domain.repository.OrderRepository;
import com.concertcomparison.domain.repository.ReservationRepository;
import com.concertcomparison.domain.repository.SeatRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * DataLoader für Mock-Daten beim Anwendungsstart.
 * 
 * Lädt Testdaten in die H2 In-Memory-Datenbank für Entwicklung und manuelles Testen.
 * Wird nur im 'default' oder 'dev' Profil ausgeführt (nicht in 'prod').
 * 
 * Mock-Daten:
 * - Concert 1: Ed Sheeran - Stadion Tour 2026 (100 Seats)
 * - Concert 2: Taylor Swift - Eras Tour 2026 (150 Seats)
 * - Reservations: ACTIVE, EXPIRED, PURCHASED
 * - Orders: Für gekaufte Tickets
 */
@Component
@Profile({"default", "dev"})
public class DataLoader implements CommandLineRunner {
    
    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);
    
    private final SeatRepository seatRepository;
    private final ReservationRepository reservationRepository;
    private final OrderRepository orderRepository;
    
    public DataLoader(
            SeatRepository seatRepository,
            ReservationRepository reservationRepository,
            OrderRepository orderRepository
    ) {
        this.seatRepository = seatRepository;
        this.reservationRepository = reservationRepository;
        this.orderRepository = orderRepository;
    }
    
    @Override
    public void run(String... args) {
        log.info("=== Loading Mock Data ===");
        
        loadConcert1Seats();
        loadConcert2Seats();
        loadReservationsAndOrders();
        
        long totalSeats = seatRepository.countByConcertId(1L) + seatRepository.countByConcertId(2L);
        long totalReservations = reservationRepository.findByStatus(com.concertcomparison.domain.model.ReservationStatus.ACTIVE).size() 
                + reservationRepository.findByStatus(com.concertcomparison.domain.model.ReservationStatus.EXPIRED).size()
                + reservationRepository.findByStatus(com.concertcomparison.domain.model.ReservationStatus.PURCHASED).size();
        long totalOrders = orderRepository.findByUserId("test_user").size() + orderRepository.findByUserId("other_user").size();
        log.info("=== Mock Data Loaded: {} seats, {} reservations, {} orders ===", 
                totalSeats, totalReservations, totalOrders);
    }
    
    /**
     * Concert 1: Ed Sheeran - Stadion Tour 2026
     * 100 Seats: 50 VIP, 30 CATEGORY_A, 20 CATEGORY_B
     */
    private void loadConcert1Seats() {
        long concertId = 1L;
        
        // VIP Section (50 Seats): 30 AVAILABLE, 12 HELD, 8 SOLD
        for (int i = 1; i <= 50; i++) {
            String row = String.valueOf((i - 1) / 10 + 1); // Reihe 1-5
            String number = String.valueOf(i);
            String seatNumber = "VIP-" + i;
            double price = 129.99;
            
            Seat seat = new Seat(concertId, seatNumber, "VIP", "Block A", row, number, price);
            
            if (i > 30 && i <= 42) {
                seat.hold("reservation-vip-" + i, 15); // HELD
            } else if (i > 42) {
                seat.hold("reservation-vip-" + i, 15);
                seat.sell(); // SOLD
            }
            
            seatRepository.save(seat);
        }
        
        // Category A (30 Seats): 20 AVAILABLE, 8 HELD, 2 SOLD
        for (int i = 1; i <= 30; i++) {
            String row = String.valueOf((i - 1) / 10 + 1); // Reihe 1-3
            String number = String.valueOf(i);
            String seatNumber = "A-" + i;
            double price = 79.99;
            
            Seat seat = new Seat(concertId, seatNumber, "CATEGORY_A", "Block B", row, number, price);
            
            if (i > 20 && i <= 28) {
                seat.hold("reservation-a-" + i, 15);
            } else if (i > 28) {
                seat.hold("reservation-a-" + i, 15);
                seat.sell();
            }
            
            seatRepository.save(seat);
        }
        
        // Category B (20 Seats): 10 AVAILABLE, 5 HELD, 5 SOLD
        for (int i = 1; i <= 20; i++) {
            String row = String.valueOf((i - 1) / 10 + 1); // Reihe 1-2
            String number = String.valueOf(i);
            String seatNumber = "B-" + i;
            double price = 49.99;
            
            Seat seat = new Seat(concertId, seatNumber, "CATEGORY_B", "Block C", row, number, price);
            
            if (i > 10 && i <= 15) {
                seat.hold("reservation-b-" + i, 15);
            } else if (i > 15) {
                seat.hold("reservation-b-" + i, 15);
                seat.sell();
            }
            
            seatRepository.save(seat);
        }
        
        log.info("Concert 1 loaded: 100 seats (50 VIP, 30 Cat-A, 20 Cat-B)");
    }
    
    /**
     * Concert 2: Taylor Swift - Eras Tour 2026
     * 150 Seats: 75 VIP, 50 CATEGORY_A, 25 CATEGORY_B
     */
    private void loadConcert2Seats() {
        long concertId = 2L;
        
        // VIP Section (75 Seats): 45 AVAILABLE, 18 HELD, 12 SOLD
        for (int i = 1; i <= 75; i++) {
            String row = String.valueOf((i - 1) / 15 + 1); // Reihe 1-5
            String number = String.valueOf(i);
            String seatNumber = "VIP-" + i;
            double price = 159.99;
            
            Seat seat = new Seat(concertId, seatNumber, "VIP", "Block A", row, number, price);
            
            if (i > 45 && i <= 63) {
                seat.hold("reservation-vip-" + i, 15);
            } else if (i > 63) {
                seat.hold("reservation-vip-" + i, 15);
                seat.sell();
            }
            
            seatRepository.save(seat);
        }
        
        // Category A (50 Seats): 30 AVAILABLE, 14 HELD, 6 SOLD
        for (int i = 1; i <= 50; i++) {
            String row = String.valueOf((i - 1) / 10 + 1); // Reihe 1-5
            String number = String.valueOf(i);
            String seatNumber = "A-" + i;
            double price = 89.99;
            
            Seat seat = new Seat(concertId, seatNumber, "CATEGORY_A", "Block B", row, number, price);
            
            if (i > 30 && i <= 44) {
                seat.hold("reservation-a-" + i, 15);
            } else if (i > 44) {
                seat.hold("reservation-a-" + i, 15);
                seat.sell();
            }
            
            seatRepository.save(seat);
        }
        
        // Category B (25 Seats): 15 AVAILABLE, 6 HELD, 4 SOLD
        for (int i = 1; i <= 25; i++) {
            String row = String.valueOf((i - 1) / 10 + 1); // Reihe 1-3
            String number = String.valueOf(i);
            String seatNumber = "B-" + i;
            double price = 59.99;
            
            Seat seat = new Seat(concertId, seatNumber, "CATEGORY_B", "Block C", row, number, price);
            
            if (i > 15 && i <= 21) {
                seat.hold("reservation-b-" + i, 15);
            } else if (i > 21) {
                seat.hold("reservation-b-" + i, 15);
                seat.sell();
            }
            
            seatRepository.save(seat);
        }
        
        log.info("Concert 2 loaded: 150 seats (75 VIP, 50 Cat-A, 25 Cat-B)");
    }
    
    /**
     * Lädt Reservations und Orders für Bruno-Tests.
     * 
     * Testdaten:
     * - Reservation ID 1: ACTIVE, User "test_user", Seat 1 (für Purchase Success Test)
     * - Reservation ID 2: EXPIRED (für Expired Test)
     * - Reservation ID 3: ACTIVE, User "other_user", Seat 3 (für Wrong User Test)
     * - Reservation ID 4: PURCHASED (mit Order ID 1)
     * - Order ID 1: User "test_user", Seat 4
     */
    @Transactional
    private void loadReservationsAndOrders() {
        // 1️⃣ ACTIVE Reservation für Success Test (User kann kaufen)
        Seat seat1 = seatRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Seat 1 nicht gefunden"));
        Reservation reservation1 = Reservation.createHold(1L, "test_user", 15);
        reservationRepository.save(reservation1); // Speichern um ID zu generieren
        seat1.hold(reservation1.getId().toString(), 15);
        seatRepository.save(seat1);
        log.info("Reservation 1 created: ACTIVE, User 'test_user', Seat 1");
        
        // 2️⃣ EXPIRED Reservation für Expired Test
        Seat seat2 = seatRepository.findById(2L)
                .orElseThrow(() -> new RuntimeException("Seat 2 nicht gefunden"));
        Reservation reservation2 = Reservation.createHold(2L, "test_user", 1); // TTL 1 Minute
        // Simuliere Ablauf: setze expiresAt auf Vergangenheit via Reflection
        try {
            java.lang.reflect.Field expiresAtField = Reservation.class.getDeclaredField("expiresAt");
            expiresAtField.setAccessible(true);
            expiresAtField.set(reservation2, java.time.LocalDateTime.now().minusMinutes(5));
        } catch (Exception e) {
            log.error("Fehler beim Setzen von expiresAt für expired Reservation", e);
        }
        reservationRepository.save(reservation2);
        log.info("Reservation 2 created: EXPIRED, User 'test_user', Seat 2");
        
        // 3️⃣ ACTIVE Reservation für Wrong User Test (anderer User)
        Seat seat3 = seatRepository.findById(3L)
                .orElseThrow(() -> new RuntimeException("Seat 3 nicht gefunden"));
        Reservation reservation3 = Reservation.createHold(3L, "other_user", 15);
        reservationRepository.save(reservation3); // Speichern um ID zu generieren
        seat3.hold(reservation3.getId().toString(), 15);
        seatRepository.save(seat3);
        log.info("Reservation 3 created: ACTIVE, User 'other_user', Seat 3");
        
        // 4️⃣ PURCHASED Reservation + Order für Order Tests
        Seat seat4 = seatRepository.findById(4L)
                .orElseThrow(() -> new RuntimeException("Seat 4 nicht gefunden"));
        Reservation reservation4 = Reservation.createHold(4L, "test_user", 15);
        reservationRepository.save(reservation4); // Speichern um ID zu generieren
        seat4.hold(reservation4.getId().toString(), 15);
        seat4.sell(); // HELD → SOLD
        reservation4.markAsPurchased(); // ACTIVE → PURCHASED
        seatRepository.save(seat4);
        reservationRepository.save(reservation4);
        
        // Order für Seat 4 erstellen
        Order order1 = Order.createFromReservation(reservation4, seat4);
        orderRepository.save(order1);
        log.info("Reservation 4 created: PURCHASED, User 'test_user', Seat 4 → Order 1 created");
        
        log.info("Loaded {} reservations and {} orders", 4, 1);
    }
}
