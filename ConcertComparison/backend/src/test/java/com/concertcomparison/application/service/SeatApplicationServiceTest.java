package com.concertcomparison.application.service;

import com.concertcomparison.domain.model.Seat;
import com.concertcomparison.domain.model.SeatStatus;
import com.concertcomparison.domain.repository.SeatRepository;
import com.concertcomparison.presentation.dto.CategoryAvailability;
import com.concertcomparison.presentation.dto.SeatAvailabilityResponseDTO;
import com.concertcomparison.presentation.dto.SeatResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit Tests für SeatApplicationService.
 * 
 * Testet Use Cases, Aggregation und DTO-Mapping.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SeatApplicationService Unit Tests")
class SeatApplicationServiceTest {
    
    @Mock
    private SeatRepository seatRepository;
    
    private SeatApplicationService seatApplicationService;
    
    @BeforeEach
    void setUp() {
        seatApplicationService = new SeatApplicationService(seatRepository);
    }
    
    @Nested
    @DisplayName("getSeatAvailability() Tests")
    class GetSeatAvailabilityTests {
        
        @Test
        @DisplayName("Gibt alle Seats für Konzert zurück")
        void getSeatAvailability_ReturnsAllSeatsForConcert() {
            // Arrange
            Long concertId = 1L;
            List<Seat> mockSeats = Arrays.asList(
                createSeat(1L, concertId, "A-1", "VIP", "Block A", SeatStatus.AVAILABLE),
                createSeat(2L, concertId, "A-2", "VIP", "Block A", SeatStatus.SOLD),
                createSeat(3L, concertId, "B-1", "CATEGORY_A", "Block B", SeatStatus.AVAILABLE),
                createSeat(4L, concertId, "B-2", "CATEGORY_A", "Block B", SeatStatus.HELD)
            );
            
            when(seatRepository.findByConcertId(concertId)).thenReturn(mockSeats);
            
            // Act
            SeatAvailabilityResponseDTO result = seatApplicationService.getSeatAvailability(concertId);
            
            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getConcertId()).isEqualTo(concertId);
            assertThat(result.getTotalSeats()).isEqualTo(4);
            assertThat(result.getAvailableSeats()).isEqualTo(2);
            assertThat(result.getSeats()).hasSize(4);
        }
        
        @Test
        @DisplayName("Aggregiert Verfügbarkeit pro Kategorie korrekt")
        void getSeatAvailability_AggregatesCorrectly() {
            // Arrange
            Long concertId = 1L;
            List<Seat> mockSeats = Arrays.asList(
                createSeat(1L, concertId, "A-1", "VIP", "Block A", SeatStatus.AVAILABLE),
                createSeat(2L, concertId, "A-2", "VIP", "Block A", SeatStatus.SOLD),
                createSeat(3L, concertId, "A-3", "VIP", "Block A", SeatStatus.HELD),
                createSeat(4L, concertId, "B-1", "CATEGORY_A", "Block B", SeatStatus.AVAILABLE),
                createSeat(5L, concertId, "B-2", "CATEGORY_A", "Block B", SeatStatus.AVAILABLE)
            );
            
            when(seatRepository.findByConcertId(concertId)).thenReturn(mockSeats);
            
            // Act
            SeatAvailabilityResponseDTO result = seatApplicationService.getSeatAvailability(concertId);
            
            // Assert: VIP Kategorie
            assertThat(result.getCategoryAvailability()).hasSize(2);
            
            CategoryAvailability vipAvailability = result.getCategoryAvailability().get("VIP");
            assertThat(vipAvailability).isNotNull();
            assertThat(vipAvailability.getTotal()).isEqualTo(3);
            assertThat(vipAvailability.getAvailable()).isEqualTo(1);
            assertThat(vipAvailability.getHeld()).isEqualTo(1);
            assertThat(vipAvailability.getSold()).isEqualTo(1);
            
            // Assert: CATEGORY_A
            CategoryAvailability categoryAAvailability = result.getCategoryAvailability().get("CATEGORY_A");
            assertThat(categoryAAvailability).isNotNull();
            assertThat(categoryAAvailability.getTotal()).isEqualTo(2);
            assertThat(categoryAAvailability.getAvailable()).isEqualTo(2);
            assertThat(categoryAAvailability.getHeld()).isEqualTo(0);
            assertThat(categoryAAvailability.getSold()).isEqualTo(0);
        }
        
        @Test
        @DisplayName("Gibt leere Response bei keinen Seats zurück")
        void getSeatAvailability_NoSeats_ReturnsEmptyResponse() {
            // Arrange
            Long concertId = 999L;
            when(seatRepository.findByConcertId(concertId)).thenReturn(List.of());
            
            // Act
            SeatAvailabilityResponseDTO result = seatApplicationService.getSeatAvailability(concertId);
            
            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getConcertId()).isEqualTo(concertId);
            assertThat(result.getTotalSeats()).isEqualTo(0);
            assertThat(result.getAvailableSeats()).isEqualTo(0);
            assertThat(result.getSeats()).isEmpty();
            assertThat(result.getCategoryAvailability()).isEmpty();
        }
        
        @Test
        @DisplayName("Keine negativen Werte in Aggregation (Acceptance Criteria)")
        void getSeatAvailability_NoNegativeValuesInAggregation() {
            // Arrange: Alle Seats verkauft
            Long concertId = 1L;
            List<Seat> allSoldSeats = Arrays.asList(
                createSeat(1L, concertId, "A-1", "VIP", "Block A", SeatStatus.SOLD),
                createSeat(2L, concertId, "A-2", "VIP", "Block A", SeatStatus.SOLD)
            );
            
            when(seatRepository.findByConcertId(concertId)).thenReturn(allSoldSeats);
            
            // Act
            SeatAvailabilityResponseDTO result = seatApplicationService.getSeatAvailability(concertId);
            
            // Assert: Keine negativen Werte
            assertThat(result.getAvailableSeats()).isGreaterThanOrEqualTo(0);
            assertThat(result.getTotalSeats()).isGreaterThanOrEqualTo(0);
            
            CategoryAvailability vipAvailability = result.getCategoryAvailability().get("VIP");
            assertThat(vipAvailability.getAvailable()).isEqualTo(0);
            assertThat(vipAvailability.getHeld()).isEqualTo(0);
            assertThat(vipAvailability.getSold()).isEqualTo(2);
            assertThat(vipAvailability.getTotal()).isEqualTo(2);
        }
        
        @Test
        @DisplayName("Mappt Seat zu DTO korrekt")
        void getSeatAvailability_MapsSeatToDTOCorrectly() {
            // Arrange
            Long concertId = 1L;
            Seat mockSeat = createSeat(1L, concertId, "A-12", "VIP", "Block A", SeatStatus.AVAILABLE);
            
            when(seatRepository.findByConcertId(concertId)).thenReturn(List.of(mockSeat));
            
            // Act
            SeatAvailabilityResponseDTO result = seatApplicationService.getSeatAvailability(concertId);
            
            // Assert
            assertThat(result.getSeats()).hasSize(1);
            SeatResponseDTO seatDTO = result.getSeats().get(0);
            
            assertThat(seatDTO.getId()).isEqualTo(1L);
            assertThat(seatDTO.getSeatNumber()).isEqualTo("A-12");
            assertThat(seatDTO.getCategory()).isEqualTo("VIP");
            assertThat(seatDTO.getBlock()).isEqualTo("Block A");
            assertThat(seatDTO.getStatus()).isEqualTo("AVAILABLE");
            assertThat(seatDTO.getStatusDisplayName()).isEqualTo("Verfügbar");
            assertThat(seatDTO.isAvailable()).isTrue();
        }
        
        @Test
        @DisplayName("Große Anzahl an Seats (Performance-Test-Vorbereitung)")
        void getSeatAvailability_LargeNumberOfSeats() {
            // Arrange: 1000 Seats simulieren
            Long concertId = 1L;
            List<Seat> largeSeats = createLargeNumberOfSeats(concertId, 1000);
            
            when(seatRepository.findByConcertId(concertId)).thenReturn(largeSeats);
            
            // Act
            long startTime = System.currentTimeMillis();
            SeatAvailabilityResponseDTO result = seatApplicationService.getSeatAvailability(concertId);
            long duration = System.currentTimeMillis() - startTime;
            
            // Assert
            assertThat(result.getTotalSeats()).isEqualTo(1000);
            assertThat(result.getSeats()).hasSize(1000);
            
            // Performance-Hinweis: Caching wird in Integration-Tests getestet
            System.out.println("Processing time for 1000 seats: " + duration + "ms");
        }
    }
    
    // ==================== HELPER METHODS ====================
    
    /**
     * Erstellt einen Mock-Seat mit allen Feldern.
     */
    private Seat createSeat(Long id, Long concertId, String seatNumber, 
                           String category, String block, SeatStatus status) {
        Seat seat = new Seat(concertId, seatNumber, category, block);
        
        // Setze ID via Reflection (da private)
        try {
            var idField = Seat.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(seat, id);
            
            // Setze Status via Reflection (für Tests)
            if (status != SeatStatus.AVAILABLE) {
                var statusField = Seat.class.getDeclaredField("status");
                statusField.setAccessible(true);
                statusField.set(seat, status);
            }
        } catch (Exception e) {
            throw new RuntimeException("Test setup failed", e);
        }
        
        return seat;
    }
    
    /**
     * Erstellt eine große Anzahl an Test-Seats für Performance-Tests.
     */
    private List<Seat> createLargeNumberOfSeats(Long concertId, int count) {
        return java.util.stream.IntStream.range(0, count)
            .mapToObj(i -> {
                String category = i < count / 2 ? "VIP" : "CATEGORY_A";
                SeatStatus status = i % 3 == 0 ? SeatStatus.SOLD : 
                                   i % 3 == 1 ? SeatStatus.HELD : SeatStatus.AVAILABLE;
                return createSeat((long) i, concertId, "SEAT-" + i, category, "Block " + (i % 10), status);
            })
            .toList();
    }
}
