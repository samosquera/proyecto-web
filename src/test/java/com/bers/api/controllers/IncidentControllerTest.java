package com.bers.api.controllers;

import com.bers.api.dtos.IncidentDtos.*;
import com.bers.domain.entities.enums.EntityType;
import com.bers.domain.entities.enums.IncidentType;
import com.bers.security.config.JwtService;
import com.bers.services.service.IncidentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(IncidentController.class)
class IncidentControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private IncidentService incidentService;
    @MockitoBean private JwtService jwtService;

    private IncidentResponse incidentResponse;

    @BeforeEach
    void setUp() {
        incidentResponse = new IncidentResponse(
                1L, "TRIP", 1L, "VEHICLE", "Engine issue", 1L, "Driver John", LocalDateTime.now()
        );
    }

    @Test
    @WithMockUser(roles = "CLERK")
    void createIncident_ShouldCreateAndReturnIncident() throws Exception {
        IncidentCreateRequest createRequest = new IncidentCreateRequest(
                EntityType.TRIP, 1L, IncidentType.VEHICLE, "Engine issue", 1L
        );
        when(incidentService.createIncident(any(IncidentCreateRequest.class)))
                .thenReturn(incidentResponse);

        mockMvc.perform(post("/api/v1/incidents")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("VEHICLE"));

        verify(incidentService).createIncident(any(IncidentCreateRequest.class));
    }

    @Test
    @WithMockUser(roles = "DRIVER")
    void getIncidentsByEntity_ShouldReturnFilteredIncidents() throws Exception {
        when(incidentService.getIncidentsByEntityTypeAndId(EntityType.TRIP, 1L))
                .thenReturn(Arrays.asList(incidentResponse));

        mockMvc.perform(get("/api/v1/incidents/entity")
                        .param("entityType", "TRIP")
                        .param("entityId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].entityType").value("TRIP"));

        verify(incidentService).getIncidentsByEntityTypeAndId(EntityType.TRIP, 1L);
    }
}