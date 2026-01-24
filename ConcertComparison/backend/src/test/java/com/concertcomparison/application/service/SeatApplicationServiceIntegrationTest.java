package com.concertcomparison.application.service;

import com.concertcomparison.domain.model.Concert;
import com.concertcomparison.domain.model.Seat;
import com.concertcomparison.domain.repository.ConcertRepository;
import com.concertcomparison.domain.repository.SeatRepository;
import com.concertcomparison.presentation.dto.AvailabilityByCategoryDTO;
import com.concertcomparison.presentation.dto.SeatAvailabilityResponseDTO;
import com.concertcomparison.presentation.dto.SeatResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration Tests für SeatApplicationService mit echter Datenbank.
 * KEINE MOCKS - Nur echte DB-Operationen!
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("SeatApplicationService Integration Tests")
class SeatApplicationServiceIntegrationTest {

    @Autowired
    private SeatApplicationService seatApplicationService;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private ConcertRepository concertRepository;

    private Concert testConcert;
    private Seat vipSeat1;
    private Seat vipSeat2;
    private Seat regularSeat;

    @BeforeEach
    void setUp() {
        // Clean up
        seatRepository.deleteAll();
        concertRepository.deleteAll();

        // Create concert in DB
        testConcert = Concert.createConcert(
            "Rock Festival",
            LocalDateTime.now().plusDays(30),
            "Olympiastadion Berlin",
            "Beste Rock Bands"
        );
        testConcert = concertRepository.save(testConcert);

        // Create seats in DB
        vipSeat1 = new Seat(testConcert.getId(), "VIP-A-1", "VIP", "Block A", "1", "1", 129.99);
        vipSeat1 = seatRepository.save(vipSeat1);

        vipSeat2 = new Seat(testConcert.getId(), "VIP-A-2", "VIP", "Block A", "1", "2", 129.99);
        vipSeat2 = seatRepository.save(vipSeat2);
        
        regularSeat = new Seat(testConcert.getId(), "REG-B-1", "CATEGORY_A", "Block B", "2", "1", 79.99);
        regularSeat = seatRepository.save(regularSeat);
    }

    @Test
    @DisplayName("getSeatAvailability sollte alle Seats mit String IDs zurückgeben")
    void getSeatAvailability_ShouldReturnAllSeats_WithStringIds() {
        // Act
        SeatAvailabilityResponseDTO result = seatApplicationService.getSeatAvailability(testConcert.getId());

        // Assert
        assertThat(result.getConcertId()).isEqualTo(String.valueOf(testConcert.getId()));
        assertThat(result.getSeats()).hasSize(3);
        
        // Prüfe String ID Conversion
        SeatResponseDTO firstSeat = result.getSeats().get(0);
        assertThat(firstSeat.getId()).isNotNull();
        assertThat(firstSeat.getCategory()).isIn("VIP", "CATEGORY_A");
        assertThat(firstSeat.getBlock()).isIn("Block A", "Block B");
        assertThat(firstSeat.getPrice()).isIn(129.99, 79.99);
        assertThat(firstSeat.getStatus()).isEqualTo("AVAILABLE");
    }

    @Test
    @DisplayName("getSeatAvailability sollte availabilityByCategory als Array zurückgeben")
    void getSeatAvailability_ShouldReturnAvailabilityByCategory_AsArray() {
        // Act
        SeatAvailabilityResponseDTO result = seatApplicationService.getSeatAvailability(testConcert.getId());

        // Assert
        List<AvailabilityByCategoryDTO> availability = result.getAvailabilityByCategory();
        assertThat(availability).hasSize(2);
        
        // Prüfe alphabetische Sortierung
        assertThat(availability.get(0).getCategory()).isEqualTo("CATEGORY_A");
        assertThat(availability.get(1).getCategory()).isEqualTo("VIP");
    }

    @Test
    @DisplayName("getSeatAvailability sollte Availability korrekt aggregieren")
    void getSeatAvailability_ShouldAggregateAvailabilityCorrectly() {
        // Arrange: Hold einen VIP Seat
        vipSeat2.hold(String.valueOf(vipSeat2.getId()), 15);
        seatRepository.save(vipSeat2);
        
        // Act
        SeatAvailabilityResponseDTO result = seatApplicationService.getSeatAvailability(testConcert.getId());

        // Assert
        AvailabilityByCategoryDTO vipAvailability = result.getAvailabilityByCategory().stream()
                .filter(a -> a.getCategory().equals("VIP"))
                .findFirst()
                .orElseThrow();
        
        assertThat(vipAvailability.getAvailable()).isEqualTo(1);
        assertThat(vipAvailability.getHeld()).isEqualTo(1);
        assertThat(vipAvailability.getSold()).isEqualTo(0);
    }

    @Test
    @DisplayName("getSeatAvailability sollte leere Liste zurückgeben wenn keine Seats vorhanden")
    void getSeatAvailability_ShouldReturnEmpty_WhenNoSeats() {
        // Arrange: Create concert without seats
        Concert emptyConcert = Concert.createConcert(
            "Empty Concert",
            LocalDateTime.now().plusDays(60),
            "Stadium",
            "No seats"
        );
        emptyConcert = concertRepository.save(emptyConcert);

        // Act
        SeatAvailabilityResponseDTO result = seatApplicationService.getSeatAvailability(emptyConcert.getId());

        // Assert
        assertThat(result.getConcertId()).isEqualTo(String.valueOf(emptyConcert.getId()));
        assertThat(result.getSeats()).isEmpty();
        assertThat(result.getAvailabilityByCategory()).isEmpty();
    }

    @Test
    @DisplayName("getSeatAvailability sollte SOLD Seats korrekt zählen")
    void getSeatAvailability_ShouldCountSoldSeatsCorrectly() {
        // Arrange: Hold and Sell one VIP seat
        vipSeat1.hold(String.valueOf(vipSeat1.getId()), 15);
        vipSeat1.sell(String.valueOf(vipSeat1.getId()));
        seatRepository.save(vipSeat1);
        
        // Act
        SeatAvailabilityResponseDTO result = seatApplicationService.getSeatAvailability(testConcert.getId());

        // Assert
        AvailabilityByCategoryDTO vipAvailability = result.getAvailabilityByCategory().stream()
                .filter(a -> a.getCategory().equals("VIP"))
                .findFirst()
                .orElseThrow();
        
        assertThat(vipAvailability.getAvailable()).isEqualTo(1);
        assertThat(vipAvailability.getHeld()).isEqualTo(0);
        assertThat(vipAvailability.getSold()).isEqualTo(1);
    }

    @Test
    @DisplayName("getSeatAvailability sollte verschiedene Status pro Kategorie korrekt aggregieren")
    void getSeatAvailability_ShouldAggregateMixedStatusesCorrectly() {
        // Arrange: Create more VIP seats with different statuses
        Seat vipSeat3 = new Seat(testConcert.getId(), "VIP-A-3", "VIP", "Block A", "1", "3", 129.99);
        vipSeat3.hold(String.valueOf(100), 15);
        vipSeat3.sell(String.valueOf(100));
        vipSeat3 = seatRepository.save(vipSeat3);
        
        Seat vipSeat4 = new Seat(testConcert.getId(), "VIP-A-4", "VIP", "Block A", "1", "4", 129.99);
        vipSeat4.hold(String.valueOf(101), 15);
        vipSeat4 = seatRepository.save(vipSeat4);

        // Act
        SeatAvailabilityResponseDTO result = seatApplicationService.getSeatAvailability(testConcert.getId());

        // Assert
        AvailabilityByCategoryDTO vipAvailability = result.getAvailabilityByCategory().stream()
                .filter(a -> a.getCategory().equals("VIP"))
                .findFirst()
                .orElseThrow();
        
        assertThat(vipAvailability.getAvailable()).isEqualTo(2);  // vipSeat1, vipSeat2
        assertThat(vipAvailability.getHeld()).isEqualTo(1);       // vipSeat4
        assertThat(vipAvailability.getSold()).isEqualTo(1);       // vipSeat3
    }
}
