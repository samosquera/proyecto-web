package com.bers.api.controllers;

import com.bers.api.dtos.BusDtos.*;
import com.bers.domain.entities.enums.BusStatus;
import com.bers.services.service.BusService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BusController.class)
@DisplayName("BusController Tests")
class BusControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper om;

    @MockitoBean
    private BusService busService;

    private BusCreateRequest createRequest;
    private BusUpdateRequest updateRequest;
    private BusResponse busResponse;
    private Map<String, Object> amenities;

    @BeforeEach
    void setUp() {
        amenities = new HashMap<>();
        amenities.put("wifi", true);
        amenities.put("airConditioning", true);
        amenities.put("bathroom", false);

        createRequest = new BusCreateRequest(
                "ABC-123",
                40,
                amenities,
                BusStatus.ACTIVE
        );

        updateRequest = new BusUpdateRequest(
                45,
                amenities,
                BusStatus.MAINTENANCE
        );

        busResponse = new BusResponse(
                1L,
                "ABC-123",
                40,
                amenities,
                BusStatus.ACTIVE.name()
        );
    }

    @Test
    @DisplayName("Debe crear un bus exitosamente")
    @WithMockUser(roles = "ADMIN")
    void shouldCreateBusSuccessfully() throws Exception {
        when(busService.createBus(any(BusCreateRequest.class))).thenReturn(busResponse);

        mvc.perform(post("/api/v1/buses")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.plate").value("ABC-123"))
                .andExpect(jsonPath("$.capacity").value(40))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(busService).createBus(any(BusCreateRequest.class));
    }

    @Test
    @DisplayName("Debe lanzar error cuando plate está vacío")
    @WithMockUser(roles = "ADMIN")
    void shouldThrowErrorWhenPlateIsBlank() throws Exception {
        BusCreateRequest invalidRequest = new BusCreateRequest(
                "", 40, amenities, BusStatus.ACTIVE
        );

        mvc.perform(post("/api/v1/buses")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(busService, never()).createBus(any());
    }

    @Test
    @DisplayName("Debe lanzar error cuando capacity es menor a 1")
    @WithMockUser(roles = "ADMIN")
    void shouldThrowErrorWhenCapacityIsLessThan1() throws Exception {
        BusCreateRequest invalidRequest = new BusCreateRequest(
                "ABC-123", 0, amenities, BusStatus.ACTIVE
        );

        mvc.perform(post("/api/v1/buses")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(busService, never()).createBus(any());
    }

    @Test
    @DisplayName("Debe actualizar un bus exitosamente")
    @WithMockUser(roles = "DISPATCHER")
    void shouldUpdateBusSuccessfully() throws Exception {
        when(busService.updateBus(eq(1L), any(BusUpdateRequest.class))).thenReturn(busResponse);

        mvc.perform(put("/api/v1/buses/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(busService).updateBus(eq(1L), any(BusUpdateRequest.class));
    }

    @Test
    @DisplayName("Debe obtener un bus por id")
    @WithMockUser
    void shouldGetBusById() throws Exception {
        when(busService.getBusById(1L)).thenReturn(busResponse);

        mvc.perform(get("/api/v1/buses/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.plate").value("ABC-123"));

        verify(busService).getBusById(1L);
    }

    @Test
    @DisplayName("Debe obtener un bus con seats")
    @WithMockUser
    void shouldGetBusWithSeats() throws Exception {
        when(busService.getBusWithSeats(1L)).thenReturn(busResponse);

        mvc.perform(get("/api/v1/buses/1/with-seats")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(busService).getBusWithSeats(1L);
    }

    @Test
    @DisplayName("Debe obtener un bus por plate")
    @WithMockUser
    void shouldGetBusByPlate() throws Exception {
        when(busService.getBusbyPlate("ABC-123")).thenReturn(busResponse);

        mvc.perform(get("/api/v1/buses/plate/ABC-123")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plate").value("ABC-123"));

        verify(busService).getBusbyPlate("ABC-123");
    }

    @Test
    @DisplayName("Debe obtener todos los buses")
    @WithMockUser(roles = "ADMIN")
    void shouldGetAllBuses() throws Exception {
        BusResponse bus2 = new BusResponse(2L, "XYZ-789", 50,
                new HashMap<>(), BusStatus.ACTIVE.name());

        when(busService.getAllBuses()).thenReturn(List.of(busResponse, bus2));

        mvc.perform(get("/api/v1/buses")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(busService).getAllBuses();
    }

    @Test
    @DisplayName("Debe obtener buses por status")
    @WithMockUser(roles = "DISPATCHER")
    void shouldGetBusesByStatus() throws Exception {
        when(busService.getBusesByStatus(BusStatus.ACTIVE)).thenReturn(List.of(busResponse));

        mvc.perform(get("/api/v1/buses/status/ACTIVE")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));

        verify(busService).getBusesByStatus(BusStatus.ACTIVE);
    }

    @Test
    @DisplayName("Debe obtener buses disponibles por capacidad mínima")
    @WithMockUser(roles = "DISPATCHER")
    void shouldGetAvailableBusesByMinCapacity() throws Exception {
        when(busService.getAvailableBuses(30)).thenReturn(List.of(busResponse));

        mvc.perform(get("/api/v1/buses/available")
                        .with(csrf())
                        .param("minCapacity", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(busService).getAvailableBuses(30);
    }

    @Test
    @DisplayName("Debe cambiar el status de un bus")
    @WithMockUser(roles = "ADMIN")
    void shouldChangeBusStatus() throws Exception {
        BusResponse maintenanceBus = new BusResponse(1L, "ABC-123", 40,
                amenities, BusStatus.MAINTENANCE.name());

        when(busService.changeBusStatus(1L, BusStatus.MAINTENANCE)).thenReturn(maintenanceBus);

        mvc.perform(patch("/api/v1/buses/1/status")
                        .with(csrf())
                        .param("status", "MAINTENANCE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("MAINTENANCE"));

        verify(busService).changeBusStatus(1L, BusStatus.MAINTENANCE);
    }

    @Test
    @DisplayName("Debe verificar si existe un plate")
    @WithMockUser
    void shouldCheckIfPlateExists() throws Exception {
        when(busService.existsByPlate("ABC-123")).thenReturn(true);

        mvc.perform(get("/api/v1/buses/exists/plate/ABC-123")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(busService).existsByPlate("ABC-123");
    }

    @Test
    @DisplayName("Debe eliminar un bus")
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteBus() throws Exception {
        doNothing().when(busService).deleteBus(1L);

        mvc.perform(delete("/api/v1/buses/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(busService).deleteBus(1L);
    }

    @Test
    @DisplayName("Debe denegar acceso cuando user no tiene role ADMIN para crear")
    @WithMockUser(roles = "PASSENGER")
    void shouldDenyAccessWhenUserLacksAdminRoleToCreate() throws Exception {
        mvc.perform(post("/api/v1/buses")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());

        verify(busService, never()).createBus(any());
    }

    @Test
    @DisplayName("Debe denegar acceso cuando user no tiene role ADMIN para eliminar")
    @WithMockUser(roles = "DISPATCHER")
    void shouldDenyAccessWhenUserLacksAdminRoleToDelete() throws Exception {
        mvc.perform(delete("/api/v1/buses/1")
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(busService, never()).deleteBus(any());
    }
}