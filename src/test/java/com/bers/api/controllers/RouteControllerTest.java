package com.bers.api.controllers;

import com.bers.api.dtos.RouteDtos.*;
import com.bers.api.dtos.StopDtos.StopResponse;
import com.bers.security.config.JwtService;
import com.bers.services.service.RouteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RouteController.class)
class RouteControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private RouteService routeService;
    @MockitoBean
    private JwtService jwtService;
    private RouteResponse routeResponse;
    private RouteCreateRequest createRequest;
    @BeforeEach
    void setUp() {
        routeResponse = new RouteResponse(
                1L,
                "BOG-TUN",
                "Bogotá - Tunja",
                "Bogotá",
                "Tunja",
                150,
                180,
                Collections.emptyList()
        );

        createRequest = new RouteCreateRequest(
                "BOG-TUN",
                "Bogotá - Tunja",
                "Bogotá",
                "Tunja",
                150,
                180
        );
    }

    // ==================== PUBLIC ENDPOINTS ====================

    @Test
    void getAllRoutes_ShouldReturnRouteList() throws Exception {
        List<RouteResponse> routes = Arrays.asList(routeResponse);
        when(routeService.getAllRoutes()).thenReturn(routes);

        mockMvc.perform(get("/api/v1/routes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].code").value("BOG-TUN"));

        verify(routeService).getAllRoutes();
    }

    @Test
    void getRouteById_ShouldReturnRoute() throws Exception {
        when(routeService.getRouteById(1L)).thenReturn(routeResponse);

        mockMvc.perform(get("/api/v1/routes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.code").value("BOG-TUN"));

        verify(routeService).getRouteById(1L);
    }

    @Test
    void getRouteStops_ShouldReturnStopList() throws Exception {
        List<StopResponse> stops = Arrays.asList(
                new StopResponse(1L, "Bogotá Terminal", 1, null, null, 1L, "Bogotá - Tunja", "BOG-TUN")
        );
        when(routeService.getStopsByRoute(1L)).thenReturn(stops);

        mockMvc.perform(get("/api/v1/routes/1/stops"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Bogotá Terminal"));

        verify(routeService).getStopsByRoute(1L);
    }

    @Test
    void searchRoutes_WithOriginAndDestination_ShouldReturnFilteredRoutes() throws Exception {
        List<RouteResponse> routes = Arrays.asList(routeResponse);
        when(routeService.searchRoutes("Bogotá", "Tunja")).thenReturn(routes);

        mockMvc.perform(get("/api/v1/routes/search")
                        .param("origin", "Bogotá")
                        .param("destination", "Tunja"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].origin").value("Bogotá"));

        verify(routeService).searchRoutes("Bogotá", "Tunja");
    }

    @Test
    void getRouteByCode_ShouldReturnRoute() throws Exception {
        when(routeService.getRouteByCode("BOG-TUN")).thenReturn(routeResponse);

        mockMvc.perform(get("/api/v1/routes/code/BOG-TUN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("BOG-TUN"));

        verify(routeService).getRouteByCode("BOG-TUN");
    }

    @Test
    void getRouteWithStops_ShouldReturnRouteWithStops() throws Exception {
        when(routeService.getRouteWithStops(1L)).thenReturn(routeResponse);

        mockMvc.perform(get("/api/v1/routes/1/with-stops"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(routeService).getRouteWithStops(1L);
    }

    // ==================== DISPATCHER/ADMIN ENDPOINTS ====================

    @Test
    @WithMockUser(roles = "DISPATCHER")
    void createRoute_ShouldCreateAndReturnRoute() throws Exception {
        when(routeService.createRoute(any(RouteCreateRequest.class))).thenReturn(routeResponse);

        mockMvc.perform(post("/api/v1/routes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("BOG-TUN"));

        verify(routeService).createRoute(any(RouteCreateRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateRoute_ShouldUpdateAndReturnRoute() throws Exception {
        RouteUpdateRequest updateRequest = new RouteUpdateRequest(
                "Bogotá - Tunja Express",
                140,
                170
        );

        when(routeService.updateRoute(eq(1L), any(RouteUpdateRequest.class))).thenReturn(routeResponse);

        mockMvc.perform(put("/api/v1/routes/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());

        verify(routeService).updateRoute(eq(1L), any(RouteUpdateRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteRoute_ShouldReturnNoContent() throws Exception {
        doNothing().when(routeService).deleteRoute(1L);

        mockMvc.perform(delete("/api/v1/routes/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(routeService).deleteRoute(1L);
    }

    @Test
    @WithMockUser(roles = "DISPATCHER")
    void checkCodeExists_ShouldReturnBoolean() throws Exception {
        when(routeService.existsByCode("BOG-TUN")).thenReturn(true);

        mockMvc.perform(get("/api/v1/routes/code/BOG-TUN/exists"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(routeService).existsByCode("BOG-TUN");
    }

    // ==================== VALIDATION TESTS ====================

    @Test
    @WithMockUser(roles = "DISPATCHER")
    void createRoute_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        RouteCreateRequest invalidRequest = new RouteCreateRequest(
                "",  // blank code
                "",  // blank name
                "",  // blank origin
                "",  // blank destination
                -1,  // negative distance
                -1   // negative duration
        );

        mockMvc.perform(post("/api/v1/routes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(routeService, never()).createRoute(any());
    }

    // ==================== SECURITY TESTS ====================

    @Test
    void createRoute_WithoutAuth_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/routes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "PASSENGER")
    void createRoute_WithPassengerRole_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(post("/api/v1/routes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "DISPATCHER")
    void deleteRoute_WithDispatcherRole_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(delete("/api/v1/routes/1")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }
}