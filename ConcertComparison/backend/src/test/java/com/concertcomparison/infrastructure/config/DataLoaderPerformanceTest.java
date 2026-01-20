package com.concertcomparison.infrastructure.config;

import com.concertcomparison.domain.model.Concert;
import com.concertcomparison.domain.model.Seat;
import com.concertcomparison.domain.repository.ConcertRepository;
import com.concertcomparison.domain.repository.SeatRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration Tests für DataLoaderPerformance.
 * 
 * Verifiziert, dass die Performance Mock-Daten korrekt geladen werden.
 */
@SpringBootTest
@ActiveProfiles("performance")
@Transactional
@DisplayName("DataLoaderPerformance Integration Tests")
class DataLoaderPerformanceTest {

    @Autowired
    private ConcertRepository concertRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Test
    @DisplayName("Performance DataLoader lädt 2 Concerts")
    void performanceDataLoader_Loads2Concerts() {
        // Act
        List<Concert> concerts = concertRepository.findAll();

        // Assert
        assertThat(concerts).hasSize(2);
        
        assertThat(concerts)
            .extracting(Concert::getName)
            .contains(
                "Ed Sheeran - Stadion Tour 2026",
                "Taylor Swift - The Eras Tour 2026"
            );
    }

    @Test
    @DisplayName("Performance DataLoader - Concert 1 hat 100 Seats")
    void performanceDataLoader_Concert1Has100Seats() {
        // Act - Finde Concert 1
        Concert concert1 = concertRepository.findAll().stream()
            .filter(c -> c.getName().contains("Ed Sheeran"))
            .findFirst()
            .orElseThrow();
        
        long seatCount = seatRepository.countByConcertId(concert1.getId());

        // Assert
        assertThat(seatCount).isEqualTo(100);
    }

    @Test
    @DisplayName("Performance DataLoader - Concert 2 hat 150 Seats")
    void performanceDataLoader_Concert2Has150Seats() {
        // Act - Finde Concert 2
        Concert concert2 = concertRepository.findAll().stream()
            .filter(c -> c.getName().contains("Taylor Swift"))
            .findFirst()
            .orElseThrow();
        
        long seatCount = seatRepository.countByConcertId(concert2.getId());

        // Assert
        assertThat(seatCount).isEqualTo(150);
    }

    @Test
    @DisplayName("Performance DataLoader - Concert 1 Seat-Verteilung")
    void performanceDataLoader_Concert1SeatDistribution() {
        // Act - Finde Concert 1
        Concert concert1 = concertRepository.findAll().stream()
            .filter(c -> c.getName().contains("Ed Sheeran"))
            .findFirst()
            .orElseThrow();
        
        List<Seat> seats = seatRepository.findByConcertId(concert1.getId());

        // Count by category
        long vipCount = seats.stream()
            .filter(s -> s.getCategory().equals("VIP"))
            .count();
        
        long catACount = seats.stream()
            .filter(s -> s.getCategory().equals("CATEGORY_A"))
            .count();
        
        long catBCount = seats.stream()
            .filter(s -> s.getCategory().equals("CATEGORY_B"))
            .count();

        // Assert
        assertThat(vipCount).isEqualTo(50);
        assertThat(catACount).isEqualTo(30);
        assertThat(catBCount).isEqualTo(20);
    }

    @Test
    @DisplayName("Performance DataLoader - Concert 2 Seat-Verteilung")
    void performanceDataLoader_Concert2SeatDistribution() {
        // Act - Finde Concert 2
        Concert concert2 = concertRepository.findAll().stream()
            .filter(c -> c.getName().contains("Taylor Swift"))
            .findFirst()
            .orElseThrow();
        
        List<Seat> seats = seatRepository.findByConcertId(concert2.getId());

        // Count by category
        long vipCount = seats.stream()
            .filter(s -> s.getCategory().equals("VIP"))
            .count();
        
        long catACount = seats.stream()
            .filter(s -> s.getCategory().equals("CATEGORY_A"))
            .count();
        
        long catBCount = seats.stream()
            .filter(s -> s.getCategory().equals("CATEGORY_B"))
            .count();

        // Assert
        assertThat(vipCount).isEqualTo(75);
        assertThat(catACount).isEqualTo(50);
        assertThat(catBCount).isEqualTo(25);
    }

    @Test
    @DisplayName("Performance DataLoader - Alle Seats sind AVAILABLE")
    void performanceDataLoader_AllSeatsAvailable() {
        // Act - Finde beide Concerts
        Concert concert1 = concertRepository.findAll().stream()
            .filter(c -> c.getName().contains("Ed Sheeran"))
            .findFirst()
            .orElseThrow();
        Concert concert2 = concertRepository.findAll().stream()
            .filter(c -> c.getName().contains("Taylor Swift"))
            .findFirst()
            .orElseThrow();
        
        List<Seat> concert1Seats = seatRepository.findByConcertId(concert1.getId());
        List<Seat> concert2Seats = seatRepository.findByConcertId(concert2.getId());

        // Assert
        assertThat(concert1Seats)
            .allMatch(seat -> seat.getStatus().toString().equals("AVAILABLE"));
        
        assertThat(concert2Seats)
            .allMatch(seat -> seat.getStatus().toString().equals("AVAILABLE"));
    }

    @Test
    @DisplayName("Performance DataLoader - Gesamt 250 Seats")
    void performanceDataLoader_Total250Seats() {
        // Act - Finde beide Concerts
        Concert concert1 = concertRepository.findAll().stream()
            .filter(c -> c.getName().contains("Ed Sheeran"))
            .findFirst()
            .orElseThrow();
        Concert concert2 = concertRepository.findAll().stream()
            .filter(c -> c.getName().contains("Taylor Swift"))
            .findFirst()
            .orElseThrow();
        
        long totalSeats = seatRepository.countByConcertId(concert1.getId()) + 
                         seatRepository.countByConcertId(concert2.getId());

        // Assert
        assertThat(totalSeats).isEqualTo(250);
    }
}
