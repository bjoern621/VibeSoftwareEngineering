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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test für DevDataLoader - Überprüft, ob Development-Daten korrekt geladen werden.
 * 
 * Diese Tests validieren:
 * - 1 Concert geladen
 * - 20 Seats geladen (10 VIP, 6 Cat-A, 4 Cat-B)
 * - Alle Seats sind AVAILABLE
 * - Korrekte Preise (VIP: 99.99, Cat-A: 69.99, Cat-B: 39.99)
 */
@SpringBootTest
@ActiveProfiles("dev")
class DataLoaderDevTest {

    @Autowired
    private ConcertRepository concertRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Test
    @DisplayName("Dev DataLoader lädt 1 Concert (zusätzlich)")
    void devDataLoader_LoadsConcert() {
        // Act
        List<Concert> concerts = concertRepository.findAll();

        // Assert - DevDataLoader fügt 1 Concert hinzu (zusätzlich zu anderen Loadern)
        assertThat(concerts)
            .filteredOn(c -> c.getName().contains("Test Concert - Development"))
            .hasSize(1);
        
        Concert devConcert = concerts.stream()
            .filter(c -> c.getName().contains("Test Concert - Development"))
            .findFirst()
            .orElseThrow();
        
        assertThat(devConcert.getVenue()).isEqualTo("Test Arena");
    }

    @Test
    @DisplayName("Dev DataLoader lädt 20 Seats für Dev Concert")
    void devDataLoader_Loads20Seats() {
        // Act - Finde das Dev Concert
        Concert devConcert = concertRepository.findAll().stream()
            .filter(c -> c.getName().contains("Test Concert - Development"))
            .findFirst()
            .orElseThrow();
        
        long seatCount = seatRepository.countByConcertId(devConcert.getId());

        // Assert
        assertThat(seatCount).isEqualTo(20);
    }

    @Test
    @DisplayName("Dev DataLoader - Korrekte Seat-Verteilung")
    void devDataLoader_CorrectSeatDistribution() {
        // Act - Finde das Dev Concert
        Concert devConcert = concertRepository.findAll().stream()
            .filter(c -> c.getName().contains("Test Concert - Development"))
            .findFirst()
            .orElseThrow();
        
        List<Seat> allSeats = seatRepository.findByConcertId(devConcert.getId());

        // Count by category
        long vipCount = allSeats.stream()
            .filter(s -> s.getCategory().equals("VIP"))
            .count();
        
        long catACount = allSeats.stream()
            .filter(s -> s.getCategory().equals("CATEGORY_A"))
            .count();
        
        long catBCount = allSeats.stream()
            .filter(s -> s.getCategory().equals("CATEGORY_B"))
            .count();

        // Assert
        assertThat(vipCount).isEqualTo(10);
        assertThat(catACount).isEqualTo(6);
        assertThat(catBCount).isEqualTo(4);
    }

    @Test
    @DisplayName("Dev DataLoader - Alle Dev-Seats sind AVAILABLE")
    void devDataLoader_AllSeatsAvailable() {
        // Act - Finde das Dev Concert
        Concert devConcert = concertRepository.findAll().stream()
            .filter(c -> c.getName().contains("Test Concert - Development"))
            .findFirst()
            .orElseThrow();
        
        List<Seat> allSeats = seatRepository.findByConcertId(devConcert.getId());

        // Assert
        assertThat(allSeats)
            .allMatch(seat -> seat.getStatus().toString().equals("AVAILABLE"));
    }

    @Test
    @DisplayName("Dev DataLoader - Korrekte Preise")
    void devDataLoader_CorrectPrices() {
        // Act - Finde das Dev Concert
        Concert devConcert = concertRepository.findAll().stream()
            .filter(c -> c.getName().contains("Test Concert - Development"))
            .findFirst()
            .orElseThrow();
        
        List<Seat> allSeats = seatRepository.findByConcertId(devConcert.getId());

        // Assert VIP Preis
        Seat vipSeat = allSeats.stream()
            .filter(s -> s.getCategory().equals("VIP"))
            .findFirst()
            .orElseThrow();
        assertThat(vipSeat.getPrice()).isEqualTo(99.99);

        // Assert Category A Preis
        Seat catASeat = allSeats.stream()
            .filter(s -> s.getCategory().equals("CATEGORY_A"))
            .findFirst()
            .orElseThrow();
        assertThat(catASeat.getPrice()).isEqualTo(69.99);

        // Assert Category B Preis
        Seat catBSeat = allSeats.stream()
            .filter(s -> s.getCategory().equals("CATEGORY_B"))
            .findFirst()
            .orElseThrow();
        assertThat(catBSeat.getPrice()).isEqualTo(39.99);
    }
}
