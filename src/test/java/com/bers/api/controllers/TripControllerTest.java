package com.bers.api.controllers;

import com.bers.api.dtos.TripDtos.*;
import com.bers.domain.entities.enums.TripStatus;
import com.bers.services.service.TripService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=none"
})
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("TripController Tests")
class TripControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper om;

    @MockitoBean
    private TripService tripService;

    private TripCreateRequest createRequest;
    private TripUpdateRequest updateRequest;
    private TripResponse tripResponse;

    @BeforeEach
    void setUp() {
        LocalDate futureDate = LocalDate.now().plusDays(7);
        LocalDateTime departure = LocalDateTime.now().plusDays(7).withHour(8).withMinute(0);
        LocalDateTime arrival = LocalDateTime.now().plusDays(7).withHour(12).withMinute(0);

        createRequest = new TripCreateRequest(
                futureDate,
                departure,
                arrival,
                1L,
                1L
        );

        updateRequest = new TripUpdateRequest(
                departure.plusHours(1),
                arrival.plusHours(1),
                2L,
                TripStatus.SCHEDULED
        );

        tripResponse = new TripResponse(
                1L,
                futureDate,
                departure,
                arrival,
                TripStatus.SCHEDULED.name(),
                1L,
                "Ruta Centro",
                "Bogotá",
                "Medellín",
                1L,
                "ABC-123",
                40
        );
    }

    @Test
    @DisplayName("Debe crear un trip exitosamente")
    @WithMockUser(roles = "ADMIN")
    void shouldCreateTripSuccessfully() throws Exception {
        when(tripService.createTrip(any(TripCreateRequest.class))).thenReturn(tripResponse);

        mvc.perform(post("/api/v1/trips")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.routeName").value("Ruta Centro"))
                .andExpect(jsonPath("$.status").value(TripStatus.SCHEDULED.name()));

        verify(tripService).createTrip(any(TripCreateRequest.class));
    }

    @Test
    @DisplayName("Debe lanzar error cuando date es en el pasado")
    @WithMockUser(roles = "ADMIN")
    void shouldThrowErrorWhenDateIsInPast() throws Exception {
        TripCreateRequest pastRequest = new TripCreateRequest(
                LocalDate.now().minusDays(1),
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().minusDays(1).plusHours(4),
                1L,
                1L
        );

        mvc.perform(post("/api/v1/trips")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(pastRequest)))
                .andExpect(status().isBadRequest());

        verify(tripService, never()).createTrip(any());
    }

    @Test
    @DisplayName("Debe actualizar un trip exitosamente")
    @WithMockUser(roles = "DISPATCHER")
    void shouldUpdateTripSuccessfully() throws Exception {
        when(tripService.updateTrip(eq(1L), any(TripUpdateRequest.class))).thenReturn(tripResponse);

        mvc.perform(put("/api/v1/trips/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(tripService).updateTrip(eq(1L), any(TripUpdateRequest.class));
    }

    @Test
    @DisplayName("Debe obtener un trip por id")
    @WithMockUser
    void shouldGetTripById() throws Exception {
        when(tripService.getTripById(1L)).thenReturn(tripResponse);

        mvc.perform(get("/api/v1/trips/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.origin").value("Bogotá"))
                .andExpect(jsonPath("$.destination").value("Medellín"));

        verify(tripService).getTripById(1L);
    }

    @Test
    @DisplayName("Debe obtener un trip con detalles")
    @WithMockUser
    void shouldGetTripWithDetails() throws Exception {
        when(tripService.getTripWithDetails(1L)).thenReturn(tripResponse);

        mvc.perform(get("/api/v1/trips/1/details")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.busPlate").value("ABC-123"));

        verify(tripService).getTripWithDetails(1L);
    }

    @Test
    @DisplayName("Debe obtener todos los trips")
    @WithMockUser
    void shouldGetAllTrips() throws Exception {
        TripResponse trip2 = new TripResponse(2L, LocalDate.now(), LocalDateTime.now(),
                LocalDateTime.now().plusHours(4), TripStatus.SCHEDULED.name(),
                2L, "Ruta Norte", "Cali", "Pasto", 2L, "XYZ-789", 45);

        when(tripService.getAllTrips()).thenReturn(List.of(tripResponse, trip2));

        mvc.perform(get("/api/v1/trips")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(tripService).getAllTrips();
    }

    @Test
    @DisplayName("Debe buscar trips con filtros")
    @WithMockUser
    void shouldSearchTripsWithFilters() throws Exception {
        when(tripService.searchTrips(eq(1L), any(LocalDate.class),any(TripStatus.class)))
                .thenReturn(List.of(tripResponse));

        mvc.perform(get("/api/v1/trips/search")
                        .with(csrf())
                        .param("routeId", "1")
                        .param("date", LocalDate.now().plusDays(7).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value(TripStatus.SCHEDULED.name()));

        verify(tripService).searchTrips(eq(1L), any(LocalDate.class),any(TripStatus.class));
    }

    @Test
    @DisplayName("Debe buscar trips sin filtros opcionales")
    @WithMockUser
    void shouldSearchTripsWithoutOptionalFilters() throws Exception {
        when(tripService.searchTrips(isNull(), isNull(),isNull()))
                .thenReturn(List.of(tripResponse));

        mvc.perform(get("/api/v1/trips/search")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(tripService).searchTrips(isNull(), isNull(),isNull());
    }

    @Test
    @DisplayName("Debe obtener trips por route y date")
    @WithMockUser
    void shouldGetTripsByRouteAndDate() throws Exception {
        when(tripService.getTripsByRouteAndDate(eq(1L), any(LocalDate.class)))
                .thenReturn(List.of(tripResponse));

        mvc.perform(get("/api/v1/trips/route/1")
                        .with(csrf())
                        .param("date", LocalDate.now().plusDays(7).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].routeId").value(1));

        verify(tripService).getTripsByRouteAndDate(eq(1L), any(LocalDate.class));
    }

    @Test
    @DisplayName("Debe obtener trips activos por bus")
    @WithMockUser
    void shouldGetActiveTripsByBus() throws Exception {
        when(tripService.getActiveTripsByBus(eq(1L), any(LocalDate.class)))
                .thenReturn(List.of(tripResponse));

        mvc.perform(get("/api/v1/trips/bus/1")
                        .with(csrf())
                        .param("date", LocalDate.now().plusDays(7).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].busId").value(1));

        verify(tripService).getActiveTripsByBus(eq(1L), any(LocalDate.class));
    }

    @Test
    @DisplayName("Debe cambiar el status de un trip")
    @WithMockUser(roles = "DISPATCHER")
    void shouldChangeTripStatus() throws Exception {
        TripResponse scheduledTrip = new TripResponse(1L, LocalDate.now(), LocalDateTime.now(),
                LocalDateTime.now().plusHours(4), TripStatus.SCHEDULED.name(),
                1L, "Ruta Centro", "Bogotá", "Medellín", 1L, "ABC-123", 40);

        when(tripService.changeTripStatus(1L, TripStatus.SCHEDULED)).thenReturn(scheduledTrip);

        mvc.perform(patch("/api/v1/trips/1/status")
                        .with(csrf())
                        .param("status", TripStatus.SCHEDULED.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(TripStatus.SCHEDULED.name()));

        verify(tripService).changeTripStatus(1L, TripStatus.SCHEDULED);
    }

    @Test
    @DisplayName("Debe eliminar un trip")
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteTrip() throws Exception {
        doNothing().when(tripService).deleteTrip(1L);

        mvc.perform(delete("/api/v1/trips/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(tripService).deleteTrip(1L);
    }

    @Test
    @DisplayName("Debe denegar acceso cuando user no tiene role requerido")
    @WithMockUser(roles = "PASSENGER")
    void shouldDenyAccessWhenUserLacksRole() throws Exception {
        mvc.perform(post("/api/v1/trips")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());

        verify(tripService, never()).createTrip(any());
    }

    @Test
    @DisplayName("Debe lanzar error cuando routeId es null")
    @WithMockUser(roles = "ADMIN")
    void shouldThrowErrorWhenRouteIdIsNull() throws Exception {
        TripCreateRequest invalidRequest = new TripCreateRequest(
                LocalDate.now().plusDays(1),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(4),
                null,
                1L
        );

        mvc.perform(post("/api/v1/trips")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(tripService, never()).createTrip(any());
    }

    @Test
    @DisplayName("Debe permitir acceso a DRIVER para cambiar status a ARRIVED")
    @WithMockUser(roles = "DRIVER")
    void shouldAllowDriverToChangeStatus() throws Exception {
        when(tripService.changeTripStatus(1L, TripStatus.ARRIVED)).thenReturn(tripResponse);

        mvc.perform(patch("/api/v1/trips/1/status")
                        .with(csrf())
                        .param("status", TripStatus.ARRIVED.name()))
                .andExpect(status().isOk());

        verify(tripService).changeTripStatus(1L, TripStatus.ARRIVED);
    }
}