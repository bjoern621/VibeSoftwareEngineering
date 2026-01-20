package com.concertcomparison.infrastructure.config;

import com.concertcomparison.domain.model.Concert;
import com.concertcomparison.domain.model.Seat;
import com.concertcomparison.domain.repository.ConcertRepository;
import com.concertcomparison.domain.repository.SeatRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * DataLoader für Entwicklung und manuelle Tests (Bruno API).
 * 
 * Lädt minimale Testdaten für schnelle Entwicklung und API-Tests.
 * Wird im 'default' oder 'dev' Profil ausgeführt (Standard).
 * 
 * Mock-Daten:
 * - Concert 1: Test Concert (20 Seats)
 */
@Component
@Profile({"default", "dev"})
public class DataLoaderDev implements CommandLineRunner {
    
    private static final Logger log = LoggerFactory.getLogger(DataLoaderDev.class);
    
    private final SeatRepository seatRepository;
    private final ConcertRepository concertRepository;
    
    public DataLoaderDev(SeatRepository seatRepository, ConcertRepository concertRepository) {
        this.seatRepository = seatRepository;
        this.concertRepository = concertRepository;
    }
    
    @Override
    public void run(String... args) {
        log.info("=== Loading Development Mock Data ===");
        
        // Concert erstellen
        Concert concert = Concert.createConcert(
            "Test Concert - Development",
            LocalDateTime.of(2026, 7, 15, 20, 0),
            "Test Arena",
            "Minimal Test-Daten für Entwicklung und Bruno API Tests"
        );
        concert = concertRepository.save(concert);
        log.info("Concert loaded: {}", concert.getName());
        
        // 20 Seats für schnelles Tests
        loadTestSeats(concert.getId());
        
        long totalSeats = seatRepository.countByConcertId(concert.getId());
        log.info("=== Development Mock Data Loaded: {} seats ===", totalSeats);
    }
    
    /**
     * Lädt 20 Test-Seats für Concert 1.
     * Verteilung: 10 VIP, 6 Category A, 4 Category B
     */
    private void loadTestSeats(Long concertId) {
        // VIP Section (10 Seats)
        for (int i = 1; i <= 10; i++) {
            String seatNumber = "VIP-" + i;
            Seat seat = new Seat(concertId, seatNumber, "VIP", "Block A", "1", String.valueOf(i), 99.99);
            seatRepository.save(seat);
        }
        
        // Category A (6 Seats)
        for (int i = 1; i <= 6; i++) {
            String seatNumber = "A-" + i;
            Seat seat = new Seat(concertId, seatNumber, "CATEGORY_A", "Block B", "2", String.valueOf(i), 69.99);
            seatRepository.save(seat);
        }
        
        // Category B (4 Seats)
        for (int i = 1; i <= 4; i++) {
            String seatNumber = "B-" + i;
            Seat seat = new Seat(concertId, seatNumber, "CATEGORY_B", "Block C", "3", String.valueOf(i), 39.99);
            seatRepository.save(seat);
        }
        
        log.info("Seats loaded: 10 VIP, 6 Category A, 4 Category B");
    }
}
