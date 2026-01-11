package com.concertcomparison.application.service;

import com.concertcomparison.domain.model.Seat;
import com.concertcomparison.domain.model.SeatStatus;
import com.concertcomparison.domain.repository.SeatRepository;
import com.concertcomparison.presentation.dto.AvailabilityByCategoryDTO;
import com.concertcomparison.presentation.dto.SeatAvailabilityResponseDTO;
import com.concertcomparison.presentation.dto.SeatResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

/**
 * Application Service Tests für Seat Availability Use Case.
 * Testet DTO-Mapping und Aggregation-Logik.
 */
@ExtendWith(MockitoExtension.class)
class SeatApplicationServiceTest {

    @Mock
    private SeatRepository seatRepository;

    @InjectMocks
    private SeatApplicationService seatApplicationService;

    private Seat vipSeat1;
    private Seat vipSeat2;
    private Seat regularSeat;

    @BeforeEach
    void setUp() {
        vipSeat1 = new Seat(1L, "VIP-A-1", "VIP", "Block A", "1", "1", 129.99);
        vipSeat1.setId(1L);

        vipSeat2 = new Seat(1L, "VIP-A-2", "VIP", "Block A", "1", "2", 129.99);
        vipSeat2.setId(2L);
        
        regularSeat = new Seat(1L, "REG-B-1", "CATEGORY_A", "Block B", "2", "1", 79.99);
        regularSeat.setId(3L);
    }

    @Test
    @DisplayName("getSeatAvailability sollte alle Seats mit String IDs zurückgeben")
    void getSeatAvailability_ShouldReturnAllSeats_WithStringIds() {
        // Arrange
        Long concertId = 1L;
        given(seatRepository.findByConcertId(concertId))
                .willReturn(Arrays.asList(vipSeat1, vipSeat2, regularSeat));

        // Act
        SeatAvailabilityResponseDTO result = seatApplicationService.getSeatAvailability(concertId);

        // Assert
        assertThat(result.getConcertId()).isEqualTo("1");
        assertThat(result.getSeats()).hasSize(3);
        
        // Prüfe String ID Conversion
        SeatResponseDTO firstSeat = result.getSeats().get(0);
        assertThat(firstSeat.getId()).isEqualTo("1");
        assertThat(firstSeat.getCategory()).isEqualTo("VIP");
        assertThat(firstSeat.getBlock()).isEqualTo("Block A");
        assertThat(firstSeat.getRow()).isEqualTo("1");
        assertThat(firstSeat.getNumber()).isEqualTo("1");
        assertThat(firstSeat.getPrice()).isEqualTo(129.99);
        assertThat(firstSeat.getStatus()).isEqualTo("AVAILABLE");
    }

    @Test
    @DisplayName("getSeatAvailability sollte availabilityByCategory als Array zurückgeben")
    void getSeatAvailability_ShouldReturnAvailabilityByCategory_AsArray() {
        // Arrange
        Long concertId = 1L;
        given(seatRepository.findByConcertId(concertId))
                .willReturn(Arrays.asList(vipSeat1, vipSeat2, regularSeat));

        // Act
        SeatAvailabilityResponseDTO result = seatApplicationService.getSeatAvailability(concertId);

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
        // Arrange
        Long concertId = 1L;
        vipSeat2.hold("res-1", java.time.LocalDateTime.now().plusMinutes(10));
        
        given(seatRepository.findByConcertId(concertId))
                .willReturn(Arrays.asList(vipSeat1, vipSeat2, regularSeat));

        // Act
        SeatAvailabilityResponseDTO result = seatApplicationService.getSeatAvailability(concertId);

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
        // Arrange
        Long concertId = 999L;
        given(seatRepository.findByConcertId(concertId))
                .willReturn(List.of());

        // Act
        SeatAvailabilityResponseDTO result = seatApplicationService.getSeatAvailability(concertId);

        // Assert
        assertThat(result.getConcertId()).isEqualTo("999");
        assertThat(result.getSeats()).isEmpty();
        assertThat(result.getAvailabilityByCategory()).isEmpty();
    }

    @Test
    @DisplayName("getSeatAvailability sollte SOLD Seats korrekt zählen")
    void getSeatAvailability_ShouldCountSoldSeatsCorrectly() {
        // Arrange
        Long concertId = 1L;
        vipSeat1.hold("res-1", java.time.LocalDateTime.now().plusMinutes(10));
        vipSeat1.sell("res-1");
        
        given(seatRepository.findByConcertId(concertId))
                .willReturn(Arrays.asList(vipSeat1, vipSeat2, regularSeat));

        // Act
        SeatAvailabilityResponseDTO result = seatApplicationService.getSeatAvailability(concertId);

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
        // Arrange
        Long concertId = 1L;
        
        Seat vipSeat3 = new Seat(1L, "VIP-A-3", "VIP", "Block A", "1", "3", 129.99);
        vipSeat3.setId(4L);
        vipSeat3.hold("res-2", java.time.LocalDateTime.now().plusMinutes(10));
        vipSeat3.sell("res-2");
        
        Seat vipSeat4 = new Seat(1L, "VIP-A-4", "VIP", "Block A", "1", "4", 129.99);
        vipSeat4.setId(5L);
        vipSeat4.hold("res-3", java.time.LocalDateTime.now().plusMinutes(10));
        
        given(seatRepository.findByConcertId(concertId))
                .willReturn(Arrays.asList(vipSeat1, vipSeat2, vipSeat3, vipSeat4));

        // Act
        SeatAvailabilityResponseDTO result = seatApplicationService.getSeatAvailability(concertId);

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
