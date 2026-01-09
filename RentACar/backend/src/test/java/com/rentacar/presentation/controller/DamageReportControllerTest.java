package com.rentacar.presentation.controller;

import com.rentacar.application.service.DamageReportApplicationService;
import com.rentacar.domain.service.TokenBlacklistService;
import com.rentacar.infrastructure.security.CustomerUserDetailsService;
import com.rentacar.infrastructure.security.JwtAuthenticationFilter;
import com.rentacar.infrastructure.security.JwtUtil;
import com.rentacar.presentation.dto.CreateDamageReportRequestDTO;
import com.rentacar.presentation.dto.DamageReportResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DamageReportController.class)
@AutoConfigureMockMvc(addFilters = false)
class DamageReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DamageReportApplicationService damageReportApplicationService;

    @MockBean
    private CustomerUserDetailsService customerUserDetailsService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private TokenBlacklistService tokenBlacklistService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void createDamageReport_Success() throws Exception {
        Long bookingId = 1L;
        CreateDamageReportRequestDTO request = new CreateDamageReportRequestDTO();
        request.setDescription("Kratzer an der Stoßstange");
        request.setEstimatedCost(new BigDecimal("150.00"));
        request.setPhotos(Arrays.asList("data:image/png;base64,test1", "data:image/png;base64,test2"));

        DamageReportResponseDTO response = new DamageReportResponseDTO(
                1L, 10L, bookingId, 5L,
                "Kratzer an der Stoßstange",
                new BigDecimal("150.00"),
                Arrays.asList("data:image/png;base64,test1", "data:image/png;base64,test2")
        );

        when(damageReportApplicationService.createDamageReport(eq(bookingId), any())).thenReturn(response);

        mockMvc.perform(post("/api/vermietung/{buchungId}/schadensbericht", bookingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value("Kratzer an der Stoßstange"))
                .andExpect(jsonPath("$.estimatedCost").value(150.00))
                .andExpect(jsonPath("$.photos").isArray())
                .andExpect(jsonPath("$.photos.length()").value(2));

        verify(damageReportApplicationService).createDamageReport(eq(bookingId), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createDamageReport_AsAdmin_Success() throws Exception {
        Long bookingId = 2L;
        CreateDamageReportRequestDTO request = new CreateDamageReportRequestDTO();
        request.setDescription("Delle in der Tür");
        request.setEstimatedCost(new BigDecimal("500.00"));

        DamageReportResponseDTO response = new DamageReportResponseDTO(
                2L, 20L, bookingId, 10L,
                "Delle in der Tür",
                new BigDecimal("500.00"),
                Collections.emptyList()
        );

        when(damageReportApplicationService.createDamageReport(eq(bookingId), any())).thenReturn(response);

        mockMvc.perform(post("/api/vermietung/{buchungId}/schadensbericht", bookingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.description").value("Delle in der Tür"));
    }

    @Test
    void createDamageReport_WithoutDescription_ValidationFails() throws Exception {
        Long bookingId = 1L;
        CreateDamageReportRequestDTO request = new CreateDamageReportRequestDTO();
        request.setDescription(""); // leere Beschreibung
        request.setEstimatedCost(new BigDecimal("100.00"));

        mockMvc.perform(post("/api/vermietung/{buchungId}/schadensbericht", bookingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void getDamageReport_Success() throws Exception {
        Long reportId = 1L;
        DamageReportResponseDTO response = new DamageReportResponseDTO(
                reportId, 10L, 5L, 3L,
                "Riss in der Windschutzscheibe",
                new BigDecimal("800.00"),
                Arrays.asList("photo1.jpg")
        );

        when(damageReportApplicationService.getDamageReport(reportId)).thenReturn(response);

        mockMvc.perform(get("/api/schadensberichte/{id}", reportId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reportId))
                .andExpect(jsonPath("$.description").value("Riss in der Windschutzscheibe"))
                .andExpect(jsonPath("$.estimatedCost").value(800.00))
                .andExpect(jsonPath("$.bookingId").value(5L))
                .andExpect(jsonPath("$.vehicleId").value(3L));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void getDamageReport_NotFound() throws Exception {
        Long reportId = 999L;
        when(damageReportApplicationService.getDamageReport(reportId))
                .thenThrow(new IllegalArgumentException("Damage report not found with ID: " + reportId));

        mockMvc.perform(get("/api/schadensberichte/{id}", reportId))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void getDamageReportsByBooking_Success() throws Exception {
        Long bookingId = 1L;
        List<DamageReportResponseDTO> reports = Arrays.asList(
                new DamageReportResponseDTO(1L, 10L, bookingId, 5L, "Kratzer", new BigDecimal("100.00"), Collections.emptyList()),
                new DamageReportResponseDTO(2L, 10L, bookingId, 5L, "Delle", new BigDecimal("200.00"), Collections.emptyList())
        );

        when(damageReportApplicationService.getDamageReportsByBooking(bookingId)).thenReturn(reports);

        mockMvc.perform(get("/api/buchungen/{buchungId}/schadensberichte", bookingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].description").value("Kratzer"))
                .andExpect(jsonPath("$[1].description").value("Delle"));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void getDamageReportsByBooking_EmptyList() throws Exception {
        Long bookingId = 99L;
        when(damageReportApplicationService.getDamageReportsByBooking(bookingId)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/buchungen/{buchungId}/schadensberichte", bookingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getDamageReportsByBooking_AsAdmin_Success() throws Exception {
        Long bookingId = 5L;
        List<DamageReportResponseDTO> reports = Arrays.asList(
                new DamageReportResponseDTO(3L, 15L, bookingId, 8L, "Glasschaden", new BigDecimal("350.00"), Collections.emptyList())
        );

        when(damageReportApplicationService.getDamageReportsByBooking(bookingId)).thenReturn(reports);

        mockMvc.perform(get("/api/buchungen/{buchungId}/schadensberichte", bookingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
}
