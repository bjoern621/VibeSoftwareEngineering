package com.rentacar.presentation.controller;

import com.rentacar.application.service.VehicleTypeApplicationService;
import com.rentacar.TestSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Tests für VehicleTypeController.
 * 
 * Testet die REST API Endpoints für Fahrzeugtypen.
 * 
 * Hinweis: Da VehicleType ein Enum ist (keine DB-Abhängigkeit),
 * verwenden wir den echten Service statt eines Mocks.
 */
@WebMvcTest(controllers = VehicleTypeController.class)
@ContextConfiguration(classes = {VehicleTypeController.class, VehicleTypeApplicationService.class, TestSecurityConfig.class})
class VehicleTypeControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void getAllVehicleTypesShouldReturnAllTypes() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/vehicle-types")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(4)))
            .andExpect(jsonPath("$[0].name", notNullValue()))
            .andExpect(jsonPath("$[0].displayName", notNullValue()))
            .andExpect(jsonPath("$[0].category", notNullValue()))
            .andExpect(jsonPath("$[0].priceClass", notNullValue()))
            .andExpect(jsonPath("$[0].dailyBaseRate", notNullValue()))
            .andExpect(jsonPath("$[0].passengerCapacity", notNullValue()));
    }
    
    @Test
    void getAllVehicleTypesShouldContainCompactCar() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/vehicle-types")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.name == 'COMPACT_CAR')]", hasSize(1)))
            .andExpect(jsonPath("$[?(@.name == 'COMPACT_CAR')].displayName", contains("Kleinwagen")))
            .andExpect(jsonPath("$[?(@.name == 'COMPACT_CAR')].category", contains("Kompaktklasse")))
            .andExpect(jsonPath("$[?(@.name == 'COMPACT_CAR')].priceClass", contains("ECONOMY")))
            .andExpect(jsonPath("$[?(@.name == 'COMPACT_CAR')].dailyBaseRate", contains(29.99)))
            .andExpect(jsonPath("$[?(@.name == 'COMPACT_CAR')].passengerCapacity", contains(5)));
    }
    
    @Test
    void getAllVehicleTypesShouldContainSedan() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/vehicle-types")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.name == 'SEDAN')]", hasSize(1)))
            .andExpect(jsonPath("$[?(@.name == 'SEDAN')].displayName", contains("Limousine")))
            .andExpect(jsonPath("$[?(@.name == 'SEDAN')].category", contains("Mittelklasse")))
            .andExpect(jsonPath("$[?(@.name == 'SEDAN')].priceClass", contains("STANDARD")));
    }
    
    @Test
    void getAllVehicleTypesShouldContainSuv() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/vehicle-types")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.name == 'SUV')]", hasSize(1)))
            .andExpect(jsonPath("$[?(@.name == 'SUV')].displayName", contains("SUV")))
            .andExpect(jsonPath("$[?(@.name == 'SUV')].category", contains("Geländewagen")))
            .andExpect(jsonPath("$[?(@.name == 'SUV')].priceClass", contains("PREMIUM")));
    }
    
    @Test
    void getAllVehicleTypesShouldContainVan() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/vehicle-types")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.name == 'VAN')]", hasSize(1)))
            .andExpect(jsonPath("$[?(@.name == 'VAN')].displayName", contains("Transporter")))
            .andExpect(jsonPath("$[?(@.name == 'VAN')].category", contains("Nutzfahrzeug")))
            .andExpect(jsonPath("$[?(@.name == 'VAN')].priceClass", contains("STANDARD")));
    }
    
    @Test
    void getVehicleTypeByNameShouldReturnCompactCar() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/vehicle-types/COMPACT_CAR")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.name", is("COMPACT_CAR")))
            .andExpect(jsonPath("$.displayName", is("Kleinwagen")))
            .andExpect(jsonPath("$.category", is("Kompaktklasse")))
            .andExpect(jsonPath("$.priceClass", is("ECONOMY")))
            .andExpect(jsonPath("$.dailyBaseRate", is(29.99)))
            .andExpect(jsonPath("$.passengerCapacity", is(5)));
    }
    
    @Test
    void getVehicleTypeByNameShouldBeCaseInsensitive() throws Exception {
        // When & Then - lowercase
        mockMvc.perform(get("/api/vehicle-types/compact_car")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name", is("COMPACT_CAR")));
        
        // When & Then - mixed case
        mockMvc.perform(get("/api/vehicle-types/CoMpAcT_CaR")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name", is("COMPACT_CAR")));
    }
    
    @Test
    void getVehicleTypeByNameShouldReturn404ForInvalidType() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/vehicle-types/INVALID_TYPE")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }
    
    @Test
    void getVehicleTypeByNameShouldReturnSedan() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/vehicle-types/SEDAN")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name", is("SEDAN")))
            .andExpect(jsonPath("$.displayName", is("Limousine")));
    }
    
    @Test
    void getVehicleTypeByNameShouldReturnSuv() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/vehicle-types/SUV")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name", is("SUV")))
            .andExpect(jsonPath("$.displayName", is("SUV")));
    }
    
    @Test
    void getVehicleTypeByNameShouldReturnVan() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/vehicle-types/VAN")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name", is("VAN")))
            .andExpect(jsonPath("$.displayName", is("Transporter")));
    }
}
