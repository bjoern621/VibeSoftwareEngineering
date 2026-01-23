package com.concertcomparison.application.service;

import com.concertcomparison.domain.exception.InvalidConcertDateException;
import com.concertcomparison.domain.model.Concert;
import com.concertcomparison.domain.model.Seat;
import com.concertcomparison.domain.repository.ConcertRepository;
import com.concertcomparison.domain.repository.SeatRepository;
import com.concertcomparison.presentation.dto.CreateConcertRequestDTO;
import com.concertcomparison.presentation.dto.CreateSeatRequestDTO;
import com.concertcomparison.presentation.dto.ConcertResponseDTO;
import com.concertcomparison.presentation.dto.UpdateConcertRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * Unit Tests für ConcertApplicationService.
 * 
 * Test-Struktur:
 * - @Nested für logische Gruppierung von Tests
 * - Mockito für Repository-Mocking
 * - AssertJ für flüssige Assertions
 * 
 * Getestete Szenarien:
 * - Concert-Erstellung mit Validierung
 * - Concert-Update mit Validierung
 * - Concert-Löschung
 * - Bulk Seat-Erstellung mit Performance
 * - Exception-Handling
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ConcertApplicationService Unit Tests")
class ConcertApplicationServiceTest {
    
    @Mock
    private ConcertRepository concertRepository;
    
    @Mock
    private SeatRepository seatRepository;
    
    @InjectMocks
    private ConcertApplicationService concertApplicationService;
    
    private static final LocalDateTime FUTURE_DATE = LocalDateTime.now().plusMonths(1);
    private static final LocalDateTime PAST_DATE = LocalDateTime.now().minusMonths(1);
    
    @Nested
    @DisplayName("createConcert - Concert-Erstellung")
    class CreateConcertTests {
        
        private CreateConcertRequestDTO validRequest;
        private Concert savedConcert;
        
        @BeforeEach
        void setUp() {
            validRequest = new CreateConcertRequestDTO(
                "Metallica Live 2025",
                FUTURE_DATE,
                "Mercedes-Benz Arena Berlin",
                "Exklusives Konzert"
            );
            
            savedConcert = Concert.createConcert(
                validRequest.getName(),
                validRequest.getDate(),
                validRequest.getVenue(),
                validRequest.getDescription()
            );
            savedConcert.setId(1L);
        }
        
        @Test
        @DisplayName("Sollte Concert erfolgreich erstellen mit gültigen Daten")
        void shouldCreateConcertSuccessfully() {
            // Arrange
            when(concertRepository.save(any(Concert.class))).thenReturn(savedConcert);
            
            // Act
            ConcertResponseDTO response = concertApplicationService.createConcert(validRequest);
            
            // Assert
            assertThat(response)
                .isNotNull()
                .extracting("name", "venue", "description")
                .containsExactly("Metallica Live 2025", "Mercedes-Benz Arena Berlin", "Exklusives Konzert");
            
            assertThat(response.getId()).isEqualTo("1");
            assertThat(response.getDate()).isEqualTo(FUTURE_DATE);
            
            verify(concertRepository, times(1)).save(any(Concert.class));
        }
        
        @Test
        @DisplayName("Sollte fehlschlagen wenn Name null ist")
        void shouldFailWhenNameIsNull() {
            // Arrange
            CreateConcertRequestDTO invalidRequest = new CreateConcertRequestDTO(
                null,
                FUTURE_DATE,
                "Mercedes-Benz Arena Berlin",
                "Description"
            );
            
            // Act & Assert
            assertThatThrownBy(() -> concertApplicationService.createConcert(invalidRequest))
                .isInstanceOf(NullPointerException.class);
            
            verify(concertRepository, never()).save(any());
        }
        
        @Test
        @DisplayName("Sollte fehlschlagen wenn Name leer ist")
        void shouldFailWhenNameIsEmpty() {
            // Arrange
            CreateConcertRequestDTO invalidRequest = new CreateConcertRequestDTO(
                "   ",
                FUTURE_DATE,
                "Mercedes-Benz Arena Berlin",
                "Description"
            );
            
            // Act & Assert
            assertThatThrownBy(() -> concertApplicationService.createConcert(invalidRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Konzert-Name darf nicht leer sein");
            
            verify(concertRepository, never()).save(any());
        }
        
        @Test
        @DisplayName("Sollte fehlschlagen wenn Datum in der Vergangenheit liegt")
        void shouldFailWhenDateIsInPast() {
            // Arrange
            CreateConcertRequestDTO invalidRequest = new CreateConcertRequestDTO(
                "Metallica Live",
                PAST_DATE,
                "Mercedes-Benz Arena Berlin",
                "Description"
            );
            
            // Act & Assert
            assertThatThrownBy(() -> concertApplicationService.createConcert(invalidRequest))
                .isInstanceOf(InvalidConcertDateException.class);
            
            verify(concertRepository, never()).save(any());
        }
        
        @Test
        @DisplayName("Sollte fehlschlagen wenn Venue null ist")
        void shouldFailWhenVenueIsNull() {
            // Arrange
            CreateConcertRequestDTO invalidRequest = new CreateConcertRequestDTO(
                "Metallica Live",
                FUTURE_DATE,
                null,
                "Description"
            );
            
            // Act & Assert
            assertThatThrownBy(() -> concertApplicationService.createConcert(invalidRequest))
                .isInstanceOf(NullPointerException.class);
            
            verify(concertRepository, never()).save(any());
        }
        
        @Test
        @DisplayName("Sollte Concert mit optionaler Description erstellen")
        void shouldCreateConcertWithOptionalDescription() {
            // Arrange
            CreateConcertRequestDTO requestWithoutDescription = new CreateConcertRequestDTO(
                "Metallica Live 2025",
                FUTURE_DATE,
                "Mercedes-Benz Arena Berlin",
                null
            );
            
            Concert concertWithoutDesc = Concert.createConcert(
                requestWithoutDescription.getName(),
                requestWithoutDescription.getDate(),
                requestWithoutDescription.getVenue(),
                requestWithoutDescription.getDescription()
            );
            concertWithoutDesc.setId(2L);
            
            when(concertRepository.save(any(Concert.class))).thenReturn(concertWithoutDesc);
            
            // Act
            ConcertResponseDTO response = concertApplicationService.createConcert(requestWithoutDescription);
            
            // Assert
            assertThat(response.getDescription()).isNull();
        }
    }
    
    @Nested
    @DisplayName("updateConcert - Concert-Update")
    class UpdateConcertTests {
        
        private UpdateConcertRequestDTO updateRequest;
        private Concert existingConcert;
        
        @BeforeEach
        void setUp() {
            updateRequest = new UpdateConcertRequestDTO(
                "Metallica Live 2025 - Updated",
                FUTURE_DATE.plusMonths(1),
                "O2 World Berlin",
                "Updated description"
            );
            
            existingConcert = Concert.createConcert(
                "Original Concert",
                FUTURE_DATE,
                "Original Venue",
                "Original description"
            );
            existingConcert.setId(1L);
        }
        
        @Test
        @DisplayName("Sollte Concert erfolgreich aktualisieren")
        void shouldUpdateConcertSuccessfully() {
            // Arrange
            Concert updatedConcert = Concert.createConcert(
                updateRequest.getName(),
                updateRequest.getDate(),
                updateRequest.getVenue(),
                updateRequest.getDescription()
            );
            updatedConcert.setId(1L);
            
            when(concertRepository.findById(1L)).thenReturn(Optional.of(existingConcert));
            when(concertRepository.save(any(Concert.class))).thenReturn(updatedConcert);
            
            // Act
            ConcertResponseDTO response = concertApplicationService.updateConcert(1L, updateRequest);
            
            // Assert
            assertThat(response)
                .extracting("name", "venue", "description")
                .containsExactly(
                    "Metallica Live 2025 - Updated",
                    "O2 World Berlin",
                    "Updated description"
                );
            
            verify(concertRepository, times(1)).findById(1L);
            verify(concertRepository, times(1)).save(any(Concert.class));
        }
        
        @Test
        @DisplayName("Sollte fehlschlagen wenn Concert nicht existiert")
        void shouldFailWhenConcertNotFound() {
            // Arrange
            when(concertRepository.findById(999L)).thenReturn(Optional.empty());
            
            // Act & Assert
            assertThatThrownBy(() -> concertApplicationService.updateConcert(999L, updateRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Concert mit ID 999 nicht gefunden");
            
            verify(concertRepository, never()).save(any());
        }
        
        @Test
        @DisplayName("Sollte fehlschlagen wenn neues Datum in der Vergangenheit liegt")
        void shouldFailWhenNewDateIsInPast() {
            // Arrange
            UpdateConcertRequestDTO invalidRequest = new UpdateConcertRequestDTO(
                "Updated Concert",
                PAST_DATE,
                "Updated Venue",
                "Updated description"
            );
            
            when(concertRepository.findById(1L)).thenReturn(Optional.of(existingConcert));
            
            // Act & Assert
            assertThatThrownBy(() -> concertApplicationService.updateConcert(1L, invalidRequest))
                .isInstanceOf(InvalidConcertDateException.class);
            
            verify(concertRepository, never()).save(any());
        }
    }
    
    @Nested
    @DisplayName("deleteConcert - Concert-Löschung")
    class DeleteConcertTests {
        
        private Concert concertToDelete;
        
        @BeforeEach
        void setUp() {
            concertToDelete = Concert.createConcert(
                "Concert to Delete",
                FUTURE_DATE,
                "Some Venue",
                "Description"
            );
            concertToDelete.setId(1L);
        }
        
        @Test
        @DisplayName("Sollte Concert erfolgreich löschen")
        void shouldDeleteConcertSuccessfully() {
            // Arrange
            when(concertRepository.findById(1L)).thenReturn(Optional.of(concertToDelete));
            when(seatRepository.findByConcertId(1L)).thenReturn(new ArrayList<>());
            
            // Act
            concertApplicationService.deleteConcert(1L);
            
            // Assert
            verify(concertRepository, times(1)).findById(1L);
        }
        
        @Test
        @DisplayName("Sollte fehlschlagen wenn Concert nicht existiert")
        void shouldFailWhenConcertNotFound() {
            // Arrange
            when(concertRepository.findById(999L)).thenReturn(Optional.empty());
            
            // Act & Assert
            assertThatThrownBy(() -> concertApplicationService.deleteConcert(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Concert mit ID 999 nicht gefunden");
        }
    }
    
    @Nested
    @DisplayName("createSeats - Bulk Seat-Erstellung")
    class CreateSeatsTests {
        
        private List<CreateSeatRequestDTO> validSeats;
        
        @BeforeEach
        void setUp() {
            validSeats = Arrays.asList(
                new CreateSeatRequestDTO("A-01", "VIP", "Block A", "A", "01", 89.99),
                new CreateSeatRequestDTO("A-02", "VIP", "Block A", "A", "02", 89.99),
                new CreateSeatRequestDTO("A-03", "VIP", "Block A", "A", "03", 89.99)
            );
        }
        
        @Test
        @DisplayName("Sollte mehrere Seats erfolgreich erstellen")
        void shouldCreateMultipleSeatsSuccessfully() {
            // Arrange
            Concert concert = Concert.createConcert(
                "Test Concert",
                FUTURE_DATE,
                "Test Venue",
                "Description"
            );
            concert.setId(1L);
            
            when(concertRepository.findById(1L)).thenReturn(Optional.of(concert));
            when(seatRepository.saveAllBatch(anyList())).thenReturn(new ArrayList<>());
            
            // Act
            concertApplicationService.createSeats(1L, validSeats);
            
            // Assert
            verify(concertRepository, times(1)).findById(1L);
            verify(seatRepository, times(1)).saveAllBatch(anyList());
        }
        
        @Test
        @DisplayName("Sollte fehlschlagen wenn keine Seats vorhanden sind")
        void shouldFailWhenNoSeatsProvided() {
            // Arrange
            List<CreateSeatRequestDTO> emptySeats = new ArrayList<>();
            
            // Act & Assert
            assertThatThrownBy(() -> concertApplicationService.createSeats(1L, emptySeats))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Mindestens 1 Sitzplatz erforderlich");
            
            verify(seatRepository, never()).saveAllBatch(anyList());
        }
        
        @Test
        @DisplayName("Sollte fehlschlagen wenn null-Liste übergeben wird")
        void shouldFailWhenNullSeatsProvided() {
            // Act & Assert
            assertThatThrownBy(() -> concertApplicationService.createSeats(1L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Mindestens 1 Sitzplatz erforderlich");
            
            verify(seatRepository, never()).saveAllBatch(anyList());
        }
        
        @Test
        @DisplayName("Sollte fehlschlagen wenn Concert nicht existiert")
        void shouldFailWhenConcertNotFound() {
            // Arrange
            when(concertRepository.findById(999L)).thenReturn(Optional.empty());
            
            // Act & Assert
            assertThatThrownBy(() -> concertApplicationService.createSeats(999L, validSeats))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Concert mit ID 999 nicht gefunden");
            
            verify(seatRepository, never()).saveAllBatch(anyList());
        }
        
        @Test
        @DisplayName("Sollte mehrere Seats in einer Operation erstellen (Batch)")
        void shouldCreateLargeNumberOfSeats() {
            // Arrange
            Concert concert = Concert.createConcert(
                "Test Concert",
                FUTURE_DATE,
                "Test Venue",
                "Description"
            );
            concert.setId(1L);
            
            // Erstelle 100 Seats für Performance-Test
            List<CreateSeatRequestDTO> manySeats = new ArrayList<>();
            for (int i = 1; i <= 100; i++) {
                manySeats.add(new CreateSeatRequestDTO(
                    "A-" + String.format("%02d", i),
                    "CATEGORY_A",
                    "Block A",
                    "A",
                    String.format("%02d", i),
                    50.0
                ));
            }
            
            when(concertRepository.findById(1L)).thenReturn(Optional.of(concert));
            when(seatRepository.saveAllBatch(anyList())).thenReturn(new ArrayList<>());
            
            // Act
            concertApplicationService.createSeats(1L, manySeats);
            
            // Assert
            verify(seatRepository, times(1)).saveAllBatch(anyList());
        }
    }
    
    @Nested
    @DisplayName("getAllConcerts - Alle Concerts abrufen")
    class GetAllConcertsTests {
        
        @Test
        @DisplayName("Sollte leere Liste zurückgeben wenn keine Concerts existieren")
        void shouldReturnEmptyListWhenNoConcertsExist() {
            // Arrange
            when(concertRepository.findAll()).thenReturn(new ArrayList<>());
            
            // Act
            List<ConcertResponseDTO> concerts = concertApplicationService.getAllConcerts();
            
            // Assert
            assertThat(concerts).isEmpty();
        }
        
        @Test
        @DisplayName("Sollte alle Concerts zurückgeben")
        void shouldReturnAllConcerts() {
            // Arrange
            Concert concert1 = Concert.createConcert("Concert 1", FUTURE_DATE, "Venue 1", "Desc 1");
            concert1.setId(1L);
            Concert concert2 = Concert.createConcert("Concert 2", FUTURE_DATE.plusMonths(1), "Venue 2", "Desc 2");
            concert2.setId(2L);
            
            when(concertRepository.findAll()).thenReturn(Arrays.asList(concert1, concert2));
            
            // Act
            List<ConcertResponseDTO> concerts = concertApplicationService.getAllConcerts();
            
            // Assert
            assertThat(concerts)
                .hasSize(2)
                .extracting("name")
                .containsExactly("Concert 1", "Concert 2");
        }
    }
}
