package com.bers.api.controllers;

import com.bers.api.dtos.StopDtos.*;
import com.bers.security.config.JwtService;
import com.bers.services.service.StopService;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StopController.class)
class StopControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private StopService stopService;
    @MockitoBean
    private JwtService jwtService;
    private StopResponse stopResponse;
    private StopCreateRequest createRequest;
    @BeforeEach
    void setUp() {
        stopResponse = new StopResponse(
                1L,
                "Terminal Bogotá",
                1,
                new BigDecimal("4.7110"),
                new BigDecimal("-74.0721"),
                1L,
                "Bogotá - Tunja",
                "BOG-TUN"
        );

        createRequest = new StopCreateRequest(
                "Terminal Bogotá",
                1,
                new BigDecimal("4.7110"),
                new BigDecimal("-74.0721"),
                1L
        );
    }

    // ==================== PUBLIC ENDPOINTS ====================

    @Test
    void getAllStops_ShouldReturnStopList() throws Exception {
        List<StopResponse> stops = Arrays.asList(stopResponse);
        when(stopService.getAllStops()).thenReturn(stops);

        mockMvc.perform(get("/api/v1/stops"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Terminal Bogotá"));

        verify(stopService).getAllStops();
    }

    @Test
    void getStopById_ShouldReturnStop() throws Exception {
        when(stopService.getStopById(1L)).thenReturn(stopResponse);

        mockMvc.perform(get("/api/v1/stops/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Terminal Bogotá"));

        verify(stopService).getStopById(1L);
    }

    @Test
    void getStopsByRoute_ShouldReturnRouteStops() throws Exception {
        List<StopResponse> stops = Arrays.asList(stopResponse);
        when(stopService.getStopsByRouteId(1L)).thenReturn(stops);

        mockMvc.perform(get("/api/v1/stops/route/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].routeId").value(1));

        verify(stopService).getStopsByRouteId(1L);
    }

    @Test
    void searchStopsByName_ShouldReturnFilteredStops() throws Exception {
        List<StopResponse> stops = Arrays.asList(stopResponse);
        when(stopService.searchStopsByName("Terminal")).thenReturn(stops);

        mockMvc.perform(get("/api/v1/stops/search")
                        .param("name", "Terminal"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Terminal Bogotá"));

        verify(stopService).searchStopsByName("Terminal");
    }

    // ==================== DISPATCHER/ADMIN ENDPOINTS ====================

    @Test
    @WithMockUser(roles = "DISPATCHER")
    void createStop_ShouldCreateAndReturnStop() throws Exception {
        when(stopService.createStop(any(StopCreateRequest.class))).thenReturn(stopResponse);

        mockMvc.perform(post("/api/v1/stops")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Terminal Bogotá"));

        verify(stopService).createStop(any(StopCreateRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateStop_ShouldUpdateAndReturnStop() throws Exception {
        StopUpdateRequest updateRequest = new StopUpdateRequest(
                "Terminal Bogotá - Norte",
                1
        );

        when(stopService.updateStop(eq(1L), any(StopUpdateRequest.class))).thenReturn(stopResponse);

        mockMvc.perform(put("/api/v1/stops/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());

        verify(stopService).updateStop(eq(1L), any(StopUpdateRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteStop_ShouldReturnNoContent() throws Exception {
        doNothing().when(stopService).deleteStop(1L);

        mockMvc.perform(delete("/api/v1/stops/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(stopService).deleteStop(1L);
    }

    // ==================== VALIDATION TESTS ====================

    @Test
    @WithMockUser(roles = "DISPATCHER")
    void createStop_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        StopCreateRequest invalidRequest = new StopCreateRequest(
                "",  // blank name
                -1,  // negative order
                new BigDecimal("100.0"),  // invalid latitude
                new BigDecimal("200.0"),  // invalid longitude
                null  // null routeId
        );

        mockMvc.perform(post("/api/v1/stops")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(stopService, never()).createStop(any());
    }

    // ==================== SECURITY TESTS ====================

    @Test
    @WithMockUser(roles = "PASSENGER")
    void createStop_WithPassengerRole_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(post("/api/v1/stops")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "DISPATCHER")
    void deleteStop_WithDispatcherRole_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(delete("/api/v1/stops/1")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }
}