package com.concertcomparison.application.service;

import com.concertcomparison.domain.model.Seat;
import com.concertcomparison.domain.model.SeatStatus;
import com.concertcomparison.domain.repository.SeatRepository;
import com.concertcomparison.presentation.dto.SeatResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * Unit Tests für SeatApplicationService - Alternative Seat Suggestion.
 * 
 * Verifies:
 * - Alternative Seats werden basierend auf Kategorie gefunden
 * - Nur verfügbare Seats werden vorgeschlagen
 * - Ausgeschlossener Seat wird gefiltert
 * - Maximal 5 Alternativen werden zurückgegeben
 * - Seats sind nach Block und Reihe sortiert
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SeatApplicationService - Alternative Seat Suggestion Tests")
class SeatApplicationServiceAlternativeTest {

    @InjectMocks
    private SeatApplicationService seatApplicationService;

    @Mock
    private SeatRepository seatRepository;

    private Long concertId = 1L;
    private String category = "VIP";
    private Long unavailableSeatId = 1L;

    @BeforeEach
    void setUp() {
        // Setup bleibt leer, wird in jedem Test spezifisch
    }

    @Test
    @DisplayName("findAlternativeSeats sollte nur verfügbare Seats zurückgeben")
    void testFindAlternativeSeatsReturnsOnlyAvailableSeats() {
        // Arrange
        List<Seat> allSeats = new ArrayList<>();
        allSeats.add(createSeat(1L, "A", "1", "1", "VIP", SeatStatus.HELD));      // HELD - sollte excluded werden
        allSeats.add(createSeat(2L, "A", "1", "2", "VIP", SeatStatus.AVAILABLE));  // AVAILABLE
        allSeats.add(createSeat(3L, "A", "1", "3", "VIP", SeatStatus.SOLD));       // SOLD - sollte excluded werden
        allSeats.add(createSeat(4L, "A", "1", "4", "VIP", SeatStatus.AVAILABLE));  // AVAILABLE

        when(seatRepository.findByConcertId(concertId)).thenReturn(allSeats);

        // Act
        List<SeatResponseDTO> alternatives = seatApplicationService.findAlternativeSeats(
                concertId, category, unavailableSeatId);

        // Assert
        assertThat(alternatives).hasSize(2);
        assertThat(alternatives).allMatch(seat -> seat.getStatus().equals("AVAILABLE"));
    }

    @Test
    @DisplayName("findAlternativeSeats sollte nur Seats der gleichen Kategorie zurückgeben")
    void testFindAlternativeSeatsFiltersByCategory() {
        // Arrange
        List<Seat> allSeats = new ArrayList<>();
        allSeats.add(createSeat(1L, "A", "1", "1", "VIP", SeatStatus.AVAILABLE));
        allSeats.add(createSeat(2L, "B", "1", "2", "STANDARD", SeatStatus.AVAILABLE)); // Different category
        allSeats.add(createSeat(3L, "A", "1", "3", "VIP", SeatStatus.AVAILABLE));
        allSeats.add(createSeat(4L, "B", "1", "4", "STANDARD", SeatStatus.AVAILABLE)); // Different category

        when(seatRepository.findByConcertId(concertId)).thenReturn(allSeats);

        // Act
        List<SeatResponseDTO> alternatives = seatApplicationService.findAlternativeSeats(
                concertId, category, unavailableSeatId);

        // Assert
        assertThat(alternatives).hasSize(1);
        assertThat(alternatives).allMatch(seat -> seat.getCategory().equals("VIP"));
    }

    @Test
    @DisplayName("findAlternativeSeats sollte den excludeSeatId aus den Ergebnissen filtern")
    void testFindAlternativeSeatsExcludesSpecificSeat() {
        // Arrange
        Long seatToExclude = 2L;
        List<Seat> allSeats = new ArrayList<>();
        allSeats.add(createSeat(1L, "A", "1", "1", "VIP", SeatStatus.AVAILABLE));
        allSeats.add(createSeat(seatToExclude, "A", "1", "2", "VIP", SeatStatus.AVAILABLE)); // Should be excluded
        allSeats.add(createSeat(3L, "A", "1", "3", "VIP", SeatStatus.AVAILABLE));

        when(seatRepository.findByConcertId(concertId)).thenReturn(allSeats);

        // Act
        List<SeatResponseDTO> alternatives = seatApplicationService.findAlternativeSeats(
                concertId, category, seatToExclude);

        // Assert
        assertThat(alternatives).hasSize(2);
        assertThat(alternatives).noneMatch(seat -> seat.getId().equals(String.valueOf(seatToExclude)));
    }

    @Test
    @DisplayName("findAlternativeSeats sollte maximal 5 Seats zurückgeben")
    void testFindAlternativeSeatsLimitedToFiveResults() {
        // Arrange
        List<Seat> allSeats = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            allSeats.add(createSeat((long) i, "A", "1", String.valueOf(i), "VIP", SeatStatus.AVAILABLE));
        }

        when(seatRepository.findByConcertId(concertId)).thenReturn(allSeats);

        // Act
        List<SeatResponseDTO> alternatives = seatApplicationService.findAlternativeSeats(
                concertId, category, unavailableSeatId);

        // Assert
        assertThat(alternatives).hasSize(5);
    }

    @Test
    @DisplayName("findAlternativeSeats sollte Seats nach Block und Reihe sortieren")
    void testFindAlternativeSeatsAreSortedByBlockAndRow() {
        // Arrange
        List<Seat> allSeats = new ArrayList<>();
        allSeats.add(createSeat(1L, "B", "2", "1", "VIP", SeatStatus.AVAILABLE));
        allSeats.add(createSeat(2L, "A", "2", "1", "VIP", SeatStatus.AVAILABLE));
        allSeats.add(createSeat(3L, "A", "1", "1", "VIP", SeatStatus.AVAILABLE));
        allSeats.add(createSeat(4L, "B", "1", "1", "VIP", SeatStatus.AVAILABLE));

        when(seatRepository.findByConcertId(concertId)).thenReturn(allSeats);

        // Act
        List<SeatResponseDTO> alternatives = seatApplicationService.findAlternativeSeats(
                concertId, category, unavailableSeatId);

        // Assert
        assertThat(alternatives).hasSize(3);
        // Verify ordering: A1, A2, B1 (Seat mit ID 1 wird ausgeschlossen)
        assertThat(alternatives.get(0).getBlock()).isEqualTo("A");
        assertThat(alternatives.get(0).getRow()).isEqualTo("1");
        assertThat(alternatives.get(1).getBlock()).isEqualTo("A");
        assertThat(alternatives.get(1).getRow()).isEqualTo("2");
        assertThat(alternatives.get(2).getBlock()).isEqualTo("B");
        assertThat(alternatives.get(2).getRow()).isEqualTo("1");
    }

    @Test
    @DisplayName("findAlternativeSeats sollte leere Liste zurückgeben wenn keine verfügbaren Seats existieren")
    void testFindAlternativeSeatsReturnsEmptyListWhenNoAvailableSeats() {
        // Arrange
        List<Seat> allSeats = new ArrayList<>();
        allSeats.add(createSeat(1L, "A", "1", "1", "VIP", SeatStatus.SOLD));
        allSeats.add(createSeat(2L, "A", "1", "2", "VIP", SeatStatus.HELD));

        when(seatRepository.findByConcertId(concertId)).thenReturn(allSeats);

        // Act
        List<SeatResponseDTO> alternatives = seatApplicationService.findAlternativeSeats(
                concertId, category, unavailableSeatId);

        // Assert
        assertThat(alternatives).isEmpty();
    }

    @Test
    @DisplayName("findAlternativeSeats sollte leere Liste zurückgeben wenn Concert keine Seats hat")
    void testFindAlternativeSeatsReturnsEmptyListWhenNoConcertSeats() {
        // Arrange
        when(seatRepository.findByConcertId(concertId)).thenReturn(new ArrayList<>());

        // Act
        List<SeatResponseDTO> alternatives = seatApplicationService.findAlternativeSeats(
                concertId, category, unavailableSeatId);

        // Assert
        assertThat(alternatives).isEmpty();
    }

    @Test
    @DisplayName("findAlternativeSeats DTO sollte alle erforderlichen Felder enthalten")
    void testFindAlternativeSeatsReturnsDTOWithAllFields() {
        // Arrange
        List<Seat> allSeats = new ArrayList<>();
        allSeats.add(createSeat(1L, "A", "1", "1", "VIP", SeatStatus.AVAILABLE));

        when(seatRepository.findByConcertId(concertId)).thenReturn(allSeats);

        // Act
        List<SeatResponseDTO> alternatives = seatApplicationService.findAlternativeSeats(
            concertId, category, 999L);

        // Assert
        assertThat(alternatives).hasSize(1);
        SeatResponseDTO dto = alternatives.get(0);
        assertThat(dto.getId()).isNotBlank();
        assertThat(dto.getBlock()).isEqualTo("A");
        assertThat(dto.getRow()).isEqualTo("1");
        assertThat(dto.getNumber()).isEqualTo("1");
        assertThat(dto.getCategory()).isEqualTo("VIP");
        assertThat(dto.getStatus()).isEqualTo("AVAILABLE");
        assertThat(dto.getPrice()).isNotNull();
    }

    // ==================== Helper Methods ====================

    private Seat createSeat(Long id, String block, String row, String number, 
                           String category, SeatStatus status) {
        Seat seat = new Seat(
                concertId,
                block + "-" + row + "-" + number,
                category,
                block,
                row,
                number,
                99.99d
        );

        seat.setId(id);

        if (status == SeatStatus.HELD) {
            seat.hold("res-" + id, 5);
        } else if (status == SeatStatus.SOLD) {
            seat.hold("res-" + id, 5);
            seat.sell();
        }

        return seat;
    }
}
