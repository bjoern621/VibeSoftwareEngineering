package com.rentacar.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rentacar.application.service.VehicleApplicationService;
import com.rentacar.domain.exception.BranchNotFoundException;
import com.rentacar.domain.exception.DuplicateLicensePlateException;
import com.rentacar.domain.exception.VehicleNotFoundException;
import com.rentacar.domain.model.VehicleStatus;
import com.rentacar.domain.model.VehicleType;
import com.rentacar.presentation.dto.CreateVehicleRequestDTO;
import com.rentacar.presentation.dto.UpdateVehicleRequestDTO;
import com.rentacar.presentation.dto.VehicleResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Tests für VehicleController.
 * 
 * Testet die REST API Endpoints mit MockMvc.
 * Verwendet @WebMvcTest für schlanke Controller-Tests.
 */
@WebMvcTest(value = VehicleController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@DisplayName("VehicleController Integration Tests")
class VehicleControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private VehicleApplicationService vehicleApplicationService;
    
    private VehicleResponseDTO testVehicleResponse;
    private CreateVehicleRequestDTO createRequest;
    private UpdateVehicleRequestDTO updateRequest;
    
    @BeforeEach
    void setUp() {
        testVehicleResponse = new VehicleResponseDTO(
            1L,
            "HH-AB 1234",
            "BMW",
            "320d",
            2023,
            15000,
            VehicleType.SEDAN,
            VehicleStatus.AVAILABLE,
            1L,
            "Hamburg Zentrum"
        );
        
        createRequest = new CreateVehicleRequestDTO(
            "HH-XY-5678",
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

    @DisplayName("POST /api/fahrzeuge - Erstellt erfolgreich ein neues Fahrzeug")
    void createVehicle_Success() throws Exception {
        // Arrange
        when(vehicleApplicationService.createVehicle(any(CreateVehicleRequestDTO.class)))
            .thenReturn(testVehicleResponse);
        
        // Act & Assert
        mockMvc.perform(post("/api/fahrzeuge")

                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id", is(1)))
            .andExpect(jsonPath("$.licensePlate", is("HH-AB 1234")))
            .andExpect(jsonPath("$.brand", is("BMW")))
            .andExpect(jsonPath("$.status", is("AVAILABLE")));
        
        verify(vehicleApplicationService).createVehicle(any(CreateVehicleRequestDTO.class));
    }
    
    @Test

    @DisplayName("POST /api/fahrzeuge - Validierungsfehler bei ungültigen Daten")
    void createVehicle_ValidationError() throws Exception {
        // Arrange - Ungültiger Request (leeres Kennzeichen)
        CreateVehicleRequestDTO invalidRequest = new CreateVehicleRequestDTO(
            "",  // Ungültig: leer
            "Audi",
            "A4",
            2024,
            5000,
            VehicleType.SEDAN,
            1L
        );
        
        // Act & Assert
        mockMvc.perform(post("/api/fahrzeuge")

                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
        
        verify(vehicleApplicationService, never()).createVehicle(any());
    }
    
    @Test

    @DisplayName("POST /api/fahrzeuge - Doppeltes Kennzeichen führt zu 400 Bad Request")
    void createVehicle_DuplicateLicensePlate() throws Exception {
        // Arrange
        when(vehicleApplicationService.createVehicle(any(CreateVehicleRequestDTO.class)))
            .thenThrow(new DuplicateLicensePlateException("HH-XY-5678"));
        
        // Act & Assert
        mockMvc.perform(post("/api/fahrzeuge")

                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", containsString("HH-XY-5678")));
        
        verify(vehicleApplicationService).createVehicle(any(CreateVehicleRequestDTO.class));
    }
    
    @Test

    @DisplayName("POST /api/fahrzeuge - Filiale nicht gefunden führt zu 404")
    void createVehicle_BranchNotFound() throws Exception {
        // Arrange
        when(vehicleApplicationService.createVehicle(any(CreateVehicleRequestDTO.class)))
            .thenThrow(new BranchNotFoundException(99L));
        
        // Act & Assert
        mockMvc.perform(post("/api/fahrzeuge")

                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message", containsString("99")));
        
        verify(vehicleApplicationService).createVehicle(any(CreateVehicleRequestDTO.class));
    }
    
    @Test

    @DisplayName("PUT /api/fahrzeuge/{id} - Aktualisiert erfolgreich ein Fahrzeug")
    void updateVehicle_Success() throws Exception {
        // Arrange
        when(vehicleApplicationService.updateVehicle(eq(1L), any(UpdateVehicleRequestDTO.class)))
            .thenReturn(testVehicleResponse);
        
        // Act & Assert
        mockMvc.perform(put("/api/fahrzeuge/1")

                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(1)))
            .andExpect(jsonPath("$.brand", is("BMW")));
        
        verify(vehicleApplicationService).updateVehicle(eq(1L), any(UpdateVehicleRequestDTO.class));
    }
    
    @Test

    @DisplayName("PUT /api/fahrzeuge/{id} - Fahrzeug nicht gefunden führt zu 404")
    void updateVehicle_NotFound() throws Exception {
        // Arrange
        when(vehicleApplicationService.updateVehicle(eq(99L), any(UpdateVehicleRequestDTO.class)))
            .thenThrow(new VehicleNotFoundException(99L));
        
        // Act & Assert
        mockMvc.perform(put("/api/fahrzeuge/99")

                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message", containsString("99")));
        
        verify(vehicleApplicationService).updateVehicle(eq(99L), any(UpdateVehicleRequestDTO.class));
    }
    
    @Test

    @DisplayName("PATCH /api/fahrzeuge/{id}/ausser-betrieb - Markiert Fahrzeug als außer Betrieb")
    void markAsOutOfService_Success() throws Exception {
        // Arrange
        doNothing().when(vehicleApplicationService).markVehicleAsOutOfService(1L);
        
        // Act & Assert
        mockMvc.perform(patch("/api/fahrzeuge/1/ausser-betrieb")
)
            .andExpect(status().isNoContent());
        
        verify(vehicleApplicationService).markVehicleAsOutOfService(1L);
    }
    
    @Test

    @DisplayName("PATCH /api/fahrzeuge/{id}/ausser-betrieb - Fahrzeug vermietet führt zu 400")
    void markAsOutOfService_VehicleRented() throws Exception {
        // Arrange
        doThrow(new IllegalStateException("Fahrzeug ist vermietet"))
            .when(vehicleApplicationService).markVehicleAsOutOfService(1L);
        
        // Act & Assert
        mockMvc.perform(patch("/api/fahrzeuge/1/ausser-betrieb")
)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", containsString("vermietet")));
        
        verify(vehicleApplicationService).markVehicleAsOutOfService(1L);
    }
    
    @Test

    @DisplayName("GET /api/fahrzeuge - Gibt alle Fahrzeuge zurück")
    void getAllVehicles_Success() throws Exception {
        // Arrange
        VehicleResponseDTO vehicle2 = new VehicleResponseDTO(
            2L, "HH-CD 9999", "Mercedes", "C-Class", 2023, 10000,
            VehicleType.SEDAN, VehicleStatus.AVAILABLE, 1L, "Hamburg Zentrum"
        );
        List<VehicleResponseDTO> vehicles = Arrays.asList(testVehicleResponse, vehicle2);
        when(vehicleApplicationService.getAllVehicles()).thenReturn(vehicles);
        
        // Act & Assert
        mockMvc.perform(get("/api/fahrzeuge")
)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].id", is(1)))
            .andExpect(jsonPath("$[1].id", is(2)));
        
        verify(vehicleApplicationService).getAllVehicles();
    }
    
    @Test

    @DisplayName("GET /api/fahrzeuge/{id} - Gibt einzelnes Fahrzeug zurück")
    void getVehicleById_Success() throws Exception {
        // Arrange
        when(vehicleApplicationService.getVehicleById(1L)).thenReturn(testVehicleResponse);
        
        // Act & Assert
        mockMvc.perform(get("/api/fahrzeuge/1")
)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(1)))
            .andExpect(jsonPath("$.licensePlate", is("HH-AB 1234")))
            .andExpect(jsonPath("$.brand", is("BMW")));
        
        verify(vehicleApplicationService).getVehicleById(1L);
    }
    
    @Test

    @DisplayName("GET /api/fahrzeuge/{id} - Fahrzeug nicht gefunden führt zu 404")
    void getVehicleById_NotFound() throws Exception {
        // Arrange
        when(vehicleApplicationService.getVehicleById(99L))
            .thenThrow(new VehicleNotFoundException(99L));
        
        // Act & Assert
        mockMvc.perform(get("/api/fahrzeuge/99")
)
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message", containsString("99")));
        
        verify(vehicleApplicationService).getVehicleById(99L);
    }
}
