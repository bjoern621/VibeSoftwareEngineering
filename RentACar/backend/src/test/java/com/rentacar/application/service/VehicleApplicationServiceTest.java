package com.rentacar.application.service;

import com.rentacar.domain.exception.BranchNotFoundException;
import com.rentacar.domain.exception.DuplicateLicensePlateException;
import com.rentacar.domain.exception.VehicleNotFoundException;
import com.rentacar.domain.exception.VehicleStatusTransitionException;
import com.rentacar.domain.model.*;
import com.rentacar.domain.repository.BranchRepository;
import com.rentacar.domain.repository.VehicleRepository;
import com.rentacar.presentation.dto.CreateVehicleRequestDTO;
import com.rentacar.presentation.dto.UpdateVehicleRequestDTO;
import com.rentacar.presentation.dto.VehicleResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit Tests für VehicleApplicationService.
 * 
 * Testet die Business-Logik und Orchestrierung der Use Cases.
 * Verwendet Mockito für Abhängigkeiten.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("VehicleApplicationService Unit Tests")
class VehicleApplicationServiceTest {
    
    @Mock
    private VehicleRepository vehicleRepository;
    
    @Mock
    private BranchRepository branchRepository;
    
    @InjectMocks
    private VehicleApplicationService vehicleApplicationService;
    
    private Branch testBranch;
    private Vehicle testVehicle;
    private CreateVehicleRequestDTO createRequest;
    private UpdateVehicleRequestDTO updateRequest;
    
    @BeforeEach
    void setUp() {
        // Test-Filiale erstellen
        testBranch = new Branch("Hamburg Zentrum", "Mönckebergstraße 1", "Mo-Fr 8-18 Uhr");
        // ID manuell setzen via Reflection für Tests
        setId(testBranch, 1L);
        
        // Test-Fahrzeug erstellen
        testVehicle = new Vehicle(
            LicensePlate.of("HH-AB 1234"),
            "BMW",
            "320d",
            2023,
            Mileage.of(15000),
            VehicleType.SEDAN,
            testBranch
        );
        setId(testVehicle, 1L);
        
        // Test-Request DTOs erstellen
        createRequest = new CreateVehicleRequestDTO(
            "HH-XY 5678",
            "Audi",
            "A4",
            2024,
            5000,
            VehicleType.SEDAN,
            1L
        );
        
        updateRequest = new UpdateVehicleRequestDTO(
            "BMW",
            "320d",
            2023,
            20000,
            VehicleType.SEDAN,
            1L
        );
    }
    
    @Test
    @DisplayName("createVehicle - Erfolgreiche Erstellung eines neuen Fahrzeugs")
    void createVehicle_Success() {
        // Arrange
        when(vehicleRepository.existsByLicensePlate(any(LicensePlate.class))).thenReturn(false);
        when(branchRepository.findById(1L)).thenReturn(Optional.of(testBranch));
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(testVehicle);
        
        // Act
        VehicleResponseDTO response = vehicleApplicationService.createVehicle(createRequest);
        
        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getBrand()).isEqualTo("BMW");
        assertThat(response.getStatus()).isEqualTo(VehicleStatus.AVAILABLE);
        
        verify(vehicleRepository).existsByLicensePlate(any(LicensePlate.class));
        verify(branchRepository).findById(1L);
        verify(vehicleRepository).save(any(Vehicle.class));
    }
    
    @Test
    @DisplayName("createVehicle - Wirft DuplicateLicensePlateException bei doppeltem Kennzeichen")
    void createVehicle_ThrowsExceptionWhenLicensePlateExists() {
        // Arrange
        when(vehicleRepository.existsByLicensePlate(any(LicensePlate.class))).thenReturn(true);
        
        // Act & Assert
        assertThatThrownBy(() -> vehicleApplicationService.createVehicle(createRequest))
            .isInstanceOf(DuplicateLicensePlateException.class)
            .hasMessageContaining("HH-XY 5678");
        
        verify(vehicleRepository).existsByLicensePlate(any(LicensePlate.class));
        verify(branchRepository, never()).findById(any());
        verify(vehicleRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("createVehicle - Wirft BranchNotFoundException wenn Filiale nicht existiert")
    void createVehicle_ThrowsExceptionWhenBranchNotFound() {
        // Arrange
        when(vehicleRepository.existsByLicensePlate(any(LicensePlate.class))).thenReturn(false);
        when(branchRepository.findById(1L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> vehicleApplicationService.createVehicle(createRequest))
            .isInstanceOf(BranchNotFoundException.class)
            .hasMessageContaining("1");
        
        verify(vehicleRepository).existsByLicensePlate(any(LicensePlate.class));
        verify(branchRepository).findById(1L);
        verify(vehicleRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("updateVehicle - Erfolgreiche Aktualisierung eines Fahrzeugs")
    void updateVehicle_Success() {
        // Arrange
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
        when(branchRepository.findById(1L)).thenReturn(Optional.of(testBranch));
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(testVehicle);
        
        // Act
        VehicleResponseDTO response = vehicleApplicationService.updateVehicle(1L, updateRequest);
        
        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getMileage()).isEqualTo(20000);
        
        verify(vehicleRepository).findById(1L);
        verify(branchRepository).findById(1L);
        verify(vehicleRepository).save(testVehicle);
    }
    
    @Test
    @DisplayName("updateVehicle - Wirft VehicleNotFoundException wenn Fahrzeug nicht existiert")
    void updateVehicle_ThrowsExceptionWhenVehicleNotFound() {
        // Arrange
        when(vehicleRepository.findById(1L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> vehicleApplicationService.updateVehicle(1L, updateRequest))
            .isInstanceOf(VehicleNotFoundException.class)
            .hasMessageContaining("1");
        
        verify(vehicleRepository).findById(1L);
        verify(branchRepository, never()).findById(any());
        verify(vehicleRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("markVehicleAsOutOfService - Erfolgreiche Ausmusterung")
    void markVehicleAsOutOfService_Success() {
        // Arrange
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(testVehicle);
        
        // Act
        vehicleApplicationService.markVehicleAsOutOfService(1L);
        
        // Assert
        assertThat(testVehicle.getStatus()).isEqualTo(VehicleStatus.OUT_OF_SERVICE);
        verify(vehicleRepository).findById(1L);
        verify(vehicleRepository).save(testVehicle);
    }
    
    @Test
    @DisplayName("markVehicleAsOutOfService - Wirft Exception wenn Fahrzeug vermietet ist")
    void markVehicleAsOutOfService_ThrowsExceptionWhenVehicleIsRented() {
        // Arrange
        testVehicle.markAsRented();
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
        
        // Act & Assert
        assertThatThrownBy(() -> vehicleApplicationService.markVehicleAsOutOfService(1L))
            .isInstanceOf(VehicleStatusTransitionException.class)
            .hasMessageContaining("vermietet");
        
        verify(vehicleRepository).findById(1L);
        verify(vehicleRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("getAllVehicles - Gibt alle Fahrzeuge zurück")
    void getAllVehicles_ReturnsAllVehicles() {
        // Arrange
        Vehicle vehicle2 = new Vehicle(
            LicensePlate.of("HH-CD 9999"),
            "Mercedes",
            "C-Class",
            2023,
            Mileage.of(10000),
            VehicleType.SEDAN,
            testBranch
        );
        setId(vehicle2, 2L);
        
        when(vehicleRepository.findAll()).thenReturn(Arrays.asList(testVehicle, vehicle2));
        
        // Act
        List<VehicleResponseDTO> responses = vehicleApplicationService.getAllVehicles();
        
        // Assert
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getId()).isEqualTo(1L);
        assertThat(responses.get(1).getId()).isEqualTo(2L);
        
        verify(vehicleRepository).findAll();
    }
    
    @Test
    @DisplayName("getVehicleById - Gibt einzelnes Fahrzeug zurück")
    void getVehicleById_ReturnsVehicle() {
        // Arrange
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
        
        // Act
        VehicleResponseDTO response = vehicleApplicationService.getVehicleById(1L);
        
        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getBrand()).isEqualTo("BMW");
        
        verify(vehicleRepository).findById(1L);
    }
    
    @Test
    @DisplayName("getVehicleById - Wirft VehicleNotFoundException wenn nicht gefunden")
    void getVehicleById_ThrowsExceptionWhenNotFound() {
        // Arrange
        when(vehicleRepository.findById(99L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> vehicleApplicationService.getVehicleById(99L))
            .isInstanceOf(VehicleNotFoundException.class)
            .hasMessageContaining("99");
        
        verify(vehicleRepository).findById(99L);
    }
    
    @Test
    @DisplayName("markVehicleAsRented - Markiert Fahrzeug als vermietet")
    void markVehicleAsRented_Success() {
        // Arrange
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(testVehicle);
        
        // Act
        vehicleApplicationService.markVehicleAsRented(1L);
        
        // Assert
        assertThat(testVehicle.getStatus()).isEqualTo(VehicleStatus.RENTED);
        verify(vehicleRepository).findById(1L);
        verify(vehicleRepository).save(testVehicle);
    }
    
    @Test
    @DisplayName("markVehicleAsRented - Wirft Exception wenn Fahrzeug nicht verfügbar")
    void markVehicleAsRented_ThrowsExceptionWhenNotAvailable() {
        // Arrange
        testVehicle.markAsRented(); // Fahrzeug ist jetzt RENTED
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
        
        // Act & Assert
        assertThatThrownBy(() -> vehicleApplicationService.markVehicleAsRented(1L))
            .isInstanceOf(VehicleStatusTransitionException.class);
        
        verify(vehicleRepository).findById(1L);
        verify(vehicleRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("markVehicleAsRented - Wirft VehicleNotFoundException wenn nicht gefunden")
    void markVehicleAsRented_ThrowsExceptionWhenNotFound() {
        // Arrange
        when(vehicleRepository.findById(99L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> vehicleApplicationService.markVehicleAsRented(99L))
            .isInstanceOf(VehicleNotFoundException.class);
        
        verify(vehicleRepository).findById(99L);
        verify(vehicleRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("returnVehicle - Gibt Fahrzeug zurück und markiert als verfügbar")
    void returnVehicle_Success() {
        // Arrange
        testVehicle.markAsRented(); // Fahrzeug muss vermietet sein
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(testVehicle);
        
        // Act
        vehicleApplicationService.returnVehicle(1L, 55000);
        
        // Assert
        assertThat(testVehicle.getStatus()).isEqualTo(VehicleStatus.AVAILABLE);
        assertThat(testVehicle.getMileage().getKilometers()).isEqualTo(55000);
        verify(vehicleRepository).findById(1L);
        verify(vehicleRepository).save(testVehicle);
    }
    
    @Test
    @DisplayName("returnVehicle - Wirft Exception wenn Fahrzeug nicht vermietet")
    void returnVehicle_ThrowsExceptionWhenNotRented() {
        // Arrange
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
        
        // Act & Assert
        assertThatThrownBy(() -> vehicleApplicationService.returnVehicle(1L, 55000))
            .isInstanceOf(VehicleStatusTransitionException.class);
        
        verify(vehicleRepository).findById(1L);
        verify(vehicleRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("returnVehicle - Wirft VehicleNotFoundException wenn nicht gefunden")
    void returnVehicle_ThrowsExceptionWhenNotFound() {
        // Arrange
        when(vehicleRepository.findById(99L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> vehicleApplicationService.returnVehicle(99L, 55000))
            .isInstanceOf(VehicleNotFoundException.class);
        
        verify(vehicleRepository).findById(99L);
        verify(vehicleRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("markVehicleAsInMaintenance - Markiert Fahrzeug als in Wartung")
    void markVehicleAsInMaintenance_Success() {
        // Arrange
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(testVehicle);
        
        // Act
        vehicleApplicationService.markVehicleAsInMaintenance(1L);
        
        // Assert
        assertThat(testVehicle.getStatus()).isEqualTo(VehicleStatus.IN_MAINTENANCE);
        verify(vehicleRepository).findById(1L);
        verify(vehicleRepository).save(testVehicle);
    }
    
    @Test
    @DisplayName("markVehicleAsInMaintenance - Wirft Exception wenn Fahrzeug vermietet")
    void markVehicleAsInMaintenance_ThrowsExceptionWhenRented() {
        // Arrange
        testVehicle.markAsRented(); // Fahrzeug ist jetzt RENTED
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
        
        // Act & Assert
        assertThatThrownBy(() -> vehicleApplicationService.markVehicleAsInMaintenance(1L))
            .isInstanceOf(VehicleStatusTransitionException.class);
        
        verify(vehicleRepository).findById(1L);
        verify(vehicleRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("markVehicleAsInMaintenance - Wirft VehicleNotFoundException wenn nicht gefunden")
    void markVehicleAsInMaintenance_ThrowsExceptionWhenNotFound() {
        // Arrange
        when(vehicleRepository.findById(99L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> vehicleApplicationService.markVehicleAsInMaintenance(99L))
            .isInstanceOf(VehicleNotFoundException.class);
        
        verify(vehicleRepository).findById(99L);
        verify(vehicleRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("reactivateVehicle - Reaktiviert Fahrzeug aus Wartung")
    void reactivateVehicle_FromMaintenance_Success() {
        // Arrange
        testVehicle.markAsInMaintenance();
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(testVehicle);
        
        // Act
        vehicleApplicationService.reactivateVehicle(1L);
        
        // Assert
        assertThat(testVehicle.getStatus()).isEqualTo(VehicleStatus.AVAILABLE);
        verify(vehicleRepository).findById(1L);
        verify(vehicleRepository).save(testVehicle);
    }
    
    @Test
    @DisplayName("reactivateVehicle - Reaktiviert Fahrzeug aus Außer-Betrieb")
    void reactivateVehicle_FromOutOfService_Success() {
        // Arrange
        testVehicle.retire();
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(testVehicle);
        
        // Act
        vehicleApplicationService.reactivateVehicle(1L);
        
        // Assert
        assertThat(testVehicle.getStatus()).isEqualTo(VehicleStatus.AVAILABLE);
        verify(vehicleRepository).findById(1L);
        verify(vehicleRepository).save(testVehicle);
    }
    
    @Test
    @DisplayName("reactivateVehicle - Wirft Exception wenn Fahrzeug bereits verfügbar")
    void reactivateVehicle_ThrowsExceptionWhenAlreadyAvailable() {
        // Arrange
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(testVehicle));
        
        // Act & Assert
        assertThatThrownBy(() -> vehicleApplicationService.reactivateVehicle(1L))
            .isInstanceOf(VehicleStatusTransitionException.class);
        
        verify(vehicleRepository).findById(1L);
        verify(vehicleRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("reactivateVehicle - Wirft VehicleNotFoundException wenn nicht gefunden")
    void reactivateVehicle_ThrowsExceptionWhenNotFound() {
        // Arrange
        when(vehicleRepository.findById(99L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> vehicleApplicationService.reactivateVehicle(99L))
            .isInstanceOf(VehicleNotFoundException.class);
        
        verify(vehicleRepository).findById(99L);
        verify(vehicleRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("searchVehicles - Findet verfügbare Fahrzeuge")
    void searchVehicles_Success() {
        // Arrange
        java.time.LocalDateTime from = java.time.LocalDateTime.now().plusDays(1);
        java.time.LocalDateTime to = java.time.LocalDateTime.now().plusDays(3);
        VehicleType type = VehicleType.SEDAN;
        String location = "Hamburg";
        
        // Verifikation: Nur verfügbare Fahrzeuge werden angezeigt (durch Mocking des Repository-Aufrufs simuliert)
        // Verifikation: Bereits gebuchte Fahrzeuge im Zeitraum werden ausgeschlossen (durch Mocking des Repository-Aufrufs simuliert) (FR8 - https://github.com/bjoern621/VibeSoftwareEngineering/issues/85)
        when(vehicleRepository.findAvailableVehicles(from, to, type, location))
            .thenReturn(Arrays.asList(testVehicle));
            
        // Act
        List<com.rentacar.presentation.dto.VehicleSearchResultDTO> results = 
            vehicleApplicationService.searchVehicles(from, to, type, location);
            
        // Assert
        assertThat(results).hasSize(1);
        // Verifikation: Ergebnis enthält Fahrzeug-Details, Verfügbarkeit und Preis pro Tag (FR8 - https://github.com/bjoern621/VibeSoftwareEngineering/issues/85)
        // 2 days * 49.99 = 99.98
        assertThat(results.get(0).getPricePerDay()).isEqualTo(new java.math.BigDecimal("49.99"));
        assertThat(results.get(0).getEstimatedTotalPrice()).isEqualTo(new java.math.BigDecimal("99.98"));
    }
    
    /**
     * Hilfsmethode zum Setzen der ID via Reflection für Tests.
     */
    private void setId(Object entity, Long id) {
        try {
            var idField = entity.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException("Fehler beim Setzen der ID", e);
        }
    }
}
