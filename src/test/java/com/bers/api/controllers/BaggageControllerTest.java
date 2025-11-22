package com.bers.api.controllers;

import com.bers.api.dtos.BaggageDtos.*;
import com.bers.security.config.JwtService;
import com.bers.services.service.BaggageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BaggageController.class)
class BaggageControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private BaggageService baggageService;
    @MockitoBean private JwtService jwtService;

    private BaggageResponse baggageResponse;

    @BeforeEach
    void setUp() {
        baggageResponse = new BaggageResponse(
                1L, new BigDecimal("25.5"), new BigDecimal("5500"),
                "BAG-12345", 1L, "John Doe", "Bogotá → Tunja", true
        );
    }

    @Test
    @WithMockUser(roles = "CLERK")
    void createBaggage_ShouldCreateAndReturnBaggage() throws Exception {
        BaggageCreateRequest createRequest = new BaggageCreateRequest(1L, new BigDecimal("25.5"));
        when(baggageService.createBaggage(any(BaggageCreateRequest.class)))
                .thenReturn(baggageResponse);

        mockMvc.perform(post("/api/v1/baggage")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.weightKg").value(25.5));

        verify(baggageService).createBaggage(any(BaggageCreateRequest.class));
    }

    @Test
    @WithMockUser(roles = "CLERK")
    void getTotalWeightByTrip_ShouldReturnTotalWeight() throws Exception {
        when(baggageService.getTotalWeightByTrip(1L)).thenReturn(new BigDecimal("120.5"));

        mockMvc.perform(get("/api/v1/baggage/trip/1/total-weight"))
                .andExpect(status().isOk())
                .andExpect(content().string("120.5"));

        verify(baggageService).getTotalWeightByTrip(1L);
    }
}
