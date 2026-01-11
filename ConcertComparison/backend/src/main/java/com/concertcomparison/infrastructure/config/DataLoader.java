package com.concertcomparison.infrastructure.config;

import com.concertcomparison.domain.model.Seat;
import com.concertcomparison.domain.model.SeatStatus;
import com.concertcomparison.domain.repository.SeatRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * DataLoader für Mock-Daten beim Anwendungsstart.
 * 
 * Lädt Testdaten in die H2 In-Memory-Datenbank für Entwicklung und manuelles Testen.
 * Wird nur im 'default' oder 'dev' Profil ausgeführt (nicht in 'prod').
 * 
 * Mock-Daten:
 * - Concert 1: Ed Sheeran - Stadion Tour 2026 (100 Seats)
 * - Concert 2: Taylor Swift - Eras Tour 2026 (150 Seats)
 */
@Component
@Profile({"default", "dev"})
public class DataLoader implements CommandLineRunner {
    
    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);
    
    private final SeatRepository seatRepository;
    
    public DataLoader(SeatRepository seatRepository) {
        this.seatRepository = seatRepository;
    }
    
    @Override
    public void run(String... args) {
        log.info("=== Loading Mock Data ===");
        
        loadConcert1Seats();
        loadConcert2Seats();
        
        long totalSeats = seatRepository.countByConcertId(1L) + seatRepository.countByConcertId(2L);
        log.info("=== Mock Data Loaded: {} total seats ===", totalSeats);
    }
    
    /**
     * Concert 1: Ed Sheeran - Stadion Tour 2026
     * 100 Seats: 50 VIP, 30 CATEGORY_A, 20 CATEGORY_B
     */
    private void loadConcert1Seats() {
        long concertId = 1L;
        
        // VIP Section (50 Seats): 30 AVAILABLE, 12 HELD, 8 SOLD
        for (int i = 1; i <= 50; i++) {
            Seat seat = new Seat(concertId, "VIP-" + i, "VIP", "Block A");
            
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
            Seat seat = new Seat(concertId, "A-" + i, "CATEGORY_A", "Block B");
            
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
            Seat seat = new Seat(concertId, "B-" + i, "CATEGORY_B", "Block C");
            
            if (i > 10 && i <= 15) {
                seat.hold("reservation-b-" + i, 15);
            } else if (i > 15) {
                seat.hold("reservation-b-" + i, 15);
                seat.sell();
            }
            
            seatRepository.save(seat);
        }
        
        log.info("Concert 1 (Ed Sheeran): 100 seats loaded");
        logConcertStats(concertId, "Ed Sheeran - Stadion Tour 2026");
    }
    
    /**
     * Concert 2: Taylor Swift - Eras Tour 2026
     * 150 Seats: 75 VIP, 50 CATEGORY_A, 25 CATEGORY_B
     */
    private void loadConcert2Seats() {
        long concertId = 2L;
        
        // VIP Section (75 Seats): 45 AVAILABLE, 20 HELD, 10 SOLD
        for (int i = 1; i <= 75; i++) {
            Seat seat = new Seat(concertId, "VIP-" + i, "VIP", "Gold Circle");
            
            if (i > 45 && i <= 65) {
                seat.hold("reservation-ts-vip-" + i, 15);
            } else if (i > 65) {
                seat.hold("reservation-ts-vip-" + i, 15);
                seat.sell();
            }
            
            seatRepository.save(seat);
        }
        
        // Category A (50 Seats): 30 AVAILABLE, 15 HELD, 5 SOLD
        for (int i = 1; i <= 50; i++) {
            Seat seat = new Seat(concertId, "A-" + i, "CATEGORY_A", "Floor Standing");
            
            if (i > 30 && i <= 45) {
                seat.hold("reservation-ts-a-" + i, 15);
            } else if (i > 45) {
                seat.hold("reservation-ts-a-" + i, 15);
                seat.sell();
            }
            
            seatRepository.save(seat);
        }
        
        // Category B (25 Seats): 15 AVAILABLE, 5 HELD, 5 SOLD
        for (int i = 1; i <= 25; i++) {
            Seat seat = new Seat(concertId, "B-" + i, "CATEGORY_B", "Upper Tier");
            
            if (i > 15 && i <= 20) {
                seat.hold("reservation-ts-b-" + i, 15);
            } else if (i > 20) {
                seat.hold("reservation-ts-b-" + i, 15);
                seat.sell();
            }
            
            seatRepository.save(seat);
        }
        
        log.info("Concert 2 (Taylor Swift): 150 seats loaded");
        logConcertStats(concertId, "Taylor Swift - Eras Tour 2026");
    }
    
    /**
     * Loggt Statistiken für ein Konzert.
     */
    private void logConcertStats(Long concertId, String concertName) {
        long total = seatRepository.countByConcertId(concertId);
        long available = seatRepository.findByConcertIdAndStatus(concertId, SeatStatus.AVAILABLE).size();
        long held = seatRepository.findByConcertIdAndStatus(concertId, SeatStatus.HELD).size();
        long sold = seatRepository.findByConcertIdAndStatus(concertId, SeatStatus.SOLD).size();
        
        log.info("  {} - Total: {}, Available: {}, Held: {}, Sold: {}", 
                 concertName, total, available, held, sold);
    }
}
