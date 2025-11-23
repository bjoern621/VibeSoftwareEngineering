package com.rentacar.presentation.controller;

import com.rentacar.application.service.BookingApplicationService;
import com.rentacar.infrastructure.security.JwtUtil;
import com.rentacar.infrastructure.security.CustomerUserDetailsService;
import com.rentacar.presentation.dto.AdditionalCostsDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerAdditionalCostsTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingApplicationService bookingApplicationService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private com.rentacar.infrastructure.security.CustomerUserDetailsService customerUserDetailsService;

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void getAdditionalCosts_ShouldReturnCosts() throws Exception {
        Long bookingId = 1L;
        AdditionalCostsDTO costs = new AdditionalCostsDTO(
                new BigDecimal("20.00"),
                new BigDecimal("30.00"),
                new BigDecimal("150.00"),
                new BigDecimal("200.00")
        );

        when(bookingApplicationService.getAdditionalCosts(bookingId)).thenReturn(costs);

        mockMvc.perform(get("/api/buchungen/{id}/zusatzkosten", bookingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lateFee").value(20.00))
                .andExpect(jsonPath("$.excessMileageFee").value(30.00))
                .andExpect(jsonPath("$.damageCost").value(150.00))
                .andExpect(jsonPath("$.totalAdditionalCost").value(200.00));
    }
}
