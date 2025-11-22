package com.bers.api.controllers;

import com.bers.api.dtos.FareRuleDtos.*;
import com.bers.domain.entities.enums.DynamicPricingStatus;
import com.bers.security.config.JwtService;
import com.bers.services.service.FareRuleService;
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
import java.util.Arrays;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FareRuleController.class)
class FareRuleControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private FareRuleService fareRuleService;
    @MockitoBean private JwtService jwtService;

    private FareRuleResponse fareRuleResponse;

    @BeforeEach
    void setUp() {
        fareRuleResponse = new FareRuleResponse(
                1L, new BigDecimal("45000"), new HashMap<>(),
                "OFF", 1L, 1L, 2L, "Bogotá", "Tunja"
        );
    }

    @Test
    void getAllFareRules_ShouldReturnFareRuleList() throws Exception {
        when(fareRuleService.getAllFareRules()).thenReturn(Arrays.asList(fareRuleResponse));

        mockMvc.perform(get("/api/v1/fare-rules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].basePrice").value(45000));

        verify(fareRuleService).getAllFareRules();
    }

    @Test
    void getFareRuleForSegment_ShouldReturnFareRule() throws Exception {
        when(fareRuleService.getFareRuleForSegment(1L, 1L, 2L)).thenReturn(fareRuleResponse);

        mockMvc.perform(get("/api/v1/fare-rules/segment")
                        .param("routeId", "1")
                        .param("fromStopId", "1")
                        .param("toStopId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fromStopName").value("Bogotá"));

        verify(fareRuleService).getFareRuleForSegment(1L, 1L, 2L);
    }

    @Test
    @WithMockUser(roles = "DISPATCHER")
    void createFareRule_ShouldCreateAndReturnFareRule() throws Exception {
        FareRuleCreateRequest createRequest = new FareRuleCreateRequest(
                new BigDecimal("45000"), new HashMap<>(), DynamicPricingStatus.OFF, 1L, 1L, 2L
        );
        when(fareRuleService.createFareRule(any(FareRuleCreateRequest.class)))
                .thenReturn(fareRuleResponse);

        mockMvc.perform(post("/api/v1/fare-rules")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.basePrice").value(45000));

        verify(fareRuleService).createFareRule(any(FareRuleCreateRequest.class));
    }
}