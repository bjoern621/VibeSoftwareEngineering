package com.rentacar.presentation.controller;

import com.rentacar.application.service.RentalApplicationService;
import com.rentacar.infrastructure.security.CustomerUserDetailsService;
import com.rentacar.infrastructure.security.JwtAuthenticationFilter;
import com.rentacar.infrastructure.security.JwtUtil;
import com.rentacar.infrastructure.security.SecurityConfig;
import org.springframework.context.annotation.Import;
import com.rentacar.presentation.dto.CheckOutRequestDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

@WebMvcTest(RentalController.class)
@AutoConfigureMockMvc(addFilters = false)
class RentalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RentalApplicationService rentalApplicationService;
    
    @MockBean
    private CustomerUserDetailsService customerUserDetailsService;
    
    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void performCheckOut_Success() throws Exception {
        Long bookingId = 1L;
        CheckOutRequestDTO request = new CheckOutRequestDTO();
        request.setMileage(1000);
        request.setFuelLevel("1/1");
        request.setCleanliness("CLEAN");
        request.setDamagesDescription("None");

        mockMvc.perform(post("/api/vermietung/{buchungId}/checkout", bookingId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(rentalApplicationService).performCheckOut(
                eq(bookingId),
                eq(1000),
                eq("1/1"),
                eq("CLEAN"),
                eq("None")
        );
    }
}
