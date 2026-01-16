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
    private final ConcertRepository concertRepository;
    
    public DataLoader(SeatRepository seatRepository, ConcertRepository concertRepository) {
        this.seatRepository = seatRepository;
        this.concertRepository = concertRepository;
    }
    
    @Override
    public void run(String... args) {
        log.info("=== Loading Mock Data ===");
        
        // Concerts zuerst erstellen
        loadConcerts();
        
        loadConcert1Seats();
        loadConcert2Seats();
        
        long totalSeats = seatRepository.countByConcertId(1L) + seatRepository.countByConcertId(2L);
        log.info("=== Mock Data Loaded: {} total seats ===", totalSeats);
    }
    
    /**
     * Erstellt die Mock-Concerts.
     */
    private void loadConcerts() {
        // Concert 1: Ed Sheeran - Stadion Tour 2026
        Concert concert1 = Concert.createConcert(
            "Ed Sheeran - Stadion Tour 2026",
            LocalDateTime.of(2026, 7, 15, 20, 0),
            "Olympiastadion Berlin",
            "Ed Sheeran live auf seiner großen Stadion-Tour 2026. Ein unvergessliches Konzert mit allen Hits!"
        );
        concertRepository.save(concert1);
        
        // Concert 2: Taylor Swift - Eras Tour 2026
        Concert concert2 = Concert.createConcert(
            "Taylor Swift - The Eras Tour 2026",
            LocalDateTime.of(2026, 8, 20, 19, 30),
            "Allianz Arena München",
            "Taylor Swift präsentiert The Eras Tour - eine spektakuläre Reise durch ihre gesamte Karriere!"
        );
        concertRepository.save(concert2);
        
        log.info("Concerts loaded: 2 concerts");
    }
    
    /**
     * Concert 1: Ed Sheeran - Stadion Tour 2026
     * 100 Seats: alle AVAILABLE (50 VIP, 30 CATEGORY_A, 20 CATEGORY_B)
     */
    private void loadConcert1Seats() {
        long concertId = 1L;
        
        // VIP Section (50 Seats): alle AVAILABLE
        for (int i = 1; i <= 50; i++) {
            String row = String.valueOf((i - 1) / 10 + 1);
            String number = String.valueOf(i);
            String seatNumber = "VIP-" + i;
            double price = 129.99;
            
            Seat seat = new Seat(concertId, seatNumber, "VIP", "Block A", row, number, price);
            // Alle Seats AVAILABLE für Load Tests
            seatRepository.save(seat);
        }
        
        // Category A (30 Seats): alle AVAILABLE
        for (int i = 1; i <= 30; i++) {
            String row = String.valueOf((i - 1) / 10 + 1);
            String number = String.valueOf(i);
            String seatNumber = "A-" + i;
            double price = 79.99;
            
            Seat seat = new Seat(concertId, seatNumber, "CATEGORY_A", "Block B", row, number, price);
            // Alle Seats AVAILABLE für Load Tests
            seatRepository.save(seat);
        }
        
        // Category B (20 Seats): alle AVAILABLE
        for (int i = 1; i <= 20; i++) {
            String row = String.valueOf((i - 1) / 10 + 1);
            String number = String.valueOf(i);
            String seatNumber = "B-" + i;
            double price = 49.99;
            
            Seat seat = new Seat(concertId, seatNumber, "CATEGORY_B", "Block C", row, number, price);
            // Alle Seats AVAILABLE für Load Tests
            seatRepository.save(seat);
        }
        
        log.info("Concert 1 loaded: 100 seats (50 VIP, 30 Cat-A, 20 Cat-B)");
    }
    
    /**
     * Concert 2: Taylor Swift - Eras Tour 2026
     * 150 Seats: alle AVAILABLE (75 VIP, 50 CATEGORY_A, 25 CATEGORY_B)
     */
    private void loadConcert2Seats() {
        long concertId = 2L;
        
        // VIP Section (75 Seats): alle AVAILABLE für Load Tests
        for (int i = 1; i <= 75; i++) {
            String row = String.valueOf((i - 1) / 10 + 1);
            String number = String.valueOf(i);
            String seatNumber = "VIP-" + i;
            double price = 149.99;
            
            Seat seat = new Seat(concertId, seatNumber, "VIP", "Block A", row, number, price);
            seatRepository.save(seat);
        }
        
        // Category A (50 Seats): alle AVAILABLE für Load Tests
        for (int i = 1; i <= 50; i++) {
            String row = String.valueOf((i - 1) / 10 + 1);
            String number = String.valueOf(i);
            String seatNumber = "A-" + i;
            double price = 89.99;
            
            Seat seat = new Seat(concertId, seatNumber, "CATEGORY_A", "Block B", row, number, price);
            seatRepository.save(seat);
        }
        
        // Category B (25 Seats): alle AVAILABLE für Load Tests
        for (int i = 1; i <= 25; i++) {
            String row = String.valueOf((i - 1) / 10 + 1);
            String number = String.valueOf(i);
            String seatNumber = "B-" + i;
            double price = 59.99;
            
            Seat seat = new Seat(concertId, seatNumber, "CATEGORY_B", "Block C", row, number, price);
            seatRepository.save(seat);
        }
        
        log.info("Concert 2 loaded: 150 seats (75 VIP, 50 Cat-A, 25 Cat-B)");
    }
}
