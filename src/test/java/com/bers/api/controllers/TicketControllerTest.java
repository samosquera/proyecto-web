package com.bers.api.controllers;

import com.bers.api.dtos.TicketDtos.*;
import com.bers.domain.entities.enums.CancellationPolicy;
import com.bers.domain.entities.enums.PaymentMethod;
import com.bers.domain.entities.enums.TicketStatus;
import com.bers.security.config.JwtService;
import com.bers.services.service.TicketService;
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
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TicketController.class)
class TicketControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private TicketService ticketService;
    @MockitoBean
    private JwtService jwtService;
    private TicketResponse ticketResponse;
    private TicketCreateRequest createRequest;
    @BeforeEach
    void setUp() {
        ticketResponse = new TicketResponse(
                1L,
                1L,
                "2025-01-15",
                "08:00",
                1L,
                "John Doe",
                1L,
                "Bogotá",
                2L,
                "Tunja",
                "A12",
                new BigDecimal("45000"),
                "CASH",
                "SOLD",
                "TICKET-123-ABC",
                LocalDateTime.now(),null,BigDecimal.ZERO,
                CancellationPolicy.NO_REFUND,
                "Bogotá",
                "Tunja",
                "xxx55"
        );

        createRequest = new TicketCreateRequest(
                1L,
                1L,
                1L,
                2L,
                "A12",
                PaymentMethod.CASH
        );
    }

    // ==================== PASSENGER/CLERK ENDPOINTS ====================

    @Test
    @WithMockUser(username = "john@example.com", roles = "PASSENGER")
    void createTicket_ShouldCreateAndReturnTicket() throws Exception {
        when(ticketService.createTicket(any(TicketCreateRequest.class))).thenReturn(ticketResponse);

        mockMvc.perform(post("/api/v1/tickets")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.seatNumber").value("A12"));

        verify(ticketService).createTicket(any(TicketCreateRequest.class));
    }

    @Test
    @WithMockUser(username = "john@example.com", roles = "PASSENGER")
    void getMyTickets_ShouldReturnUserTickets() throws Exception {
        List<TicketResponse> tickets = Arrays.asList(ticketResponse);
        when(ticketService.getTicketsByPassengerId(anyLong())).thenReturn(tickets);

        mockMvc.perform(get("/api/v1/tickets/my-tickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(ticketService).getTicketsByPassengerId(anyLong());
    }

    @Test
    @WithMockUser(roles = "PASSENGER")
    void cancelTicket_ShouldCancelAndReturnTicket() throws Exception {
        TicketResponse cancelledResponse = new TicketResponse(
                1L, 1L, "2025-01-15", "08:00", 1L, "John Doe",
                1L, "Bogotá", 2L, "Tunja", "A12",
                new BigDecimal("45000"), "CASH", "CANCELLED",
                "TICKET-123-ABC", LocalDateTime.now(),null,BigDecimal.ZERO,
                CancellationPolicy.NO_REFUND,"Bogotá","Tunja","xxx55"
        );
        when(ticketService.cancelTicket(1L)).thenReturn(cancelledResponse);

        mockMvc.perform(post("/api/v1/tickets/1/cancel")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        verify(ticketService).cancelTicket(1L);
    }

    // ==================== CLERK/ADMIN ENDPOINTS ====================

    @Test
    @WithMockUser(roles = "CLERK")
    void getAllTickets_ShouldReturnTicketList() throws Exception {
        List<TicketResponse> tickets = Arrays.asList(ticketResponse);
        when(ticketService.getAllTickets()).thenReturn(tickets);

        mockMvc.perform(get("/api/v1/tickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(ticketService).getAllTickets();
    }

    @Test
    @WithMockUser(roles = "CLERK")
    void getTicketById_ShouldReturnTicket() throws Exception {
        when(ticketService.getTicketById(1L)).thenReturn(ticketResponse);

        mockMvc.perform(get("/api/v1/tickets/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(ticketService).getTicketById(1L);
    }

    @Test
    @WithMockUser(roles = "DRIVER")
    void getTicketWithDetails_ShouldReturnTicketWithDetails() throws Exception {
        when(ticketService.getTicketWithDetails(1L)).thenReturn(ticketResponse);

        mockMvc.perform(get("/api/v1/tickets/1/details"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(ticketService).getTicketWithDetails(1L);
    }

    @Test
    @WithMockUser(roles = "CLERK")
    void getTicketByQrCode_ShouldReturnTicket() throws Exception {
        when(ticketService.getTicketByQrCode("TICKET-123-ABC")).thenReturn(ticketResponse);

        mockMvc.perform(get("/api/v1/tickets/qr/TICKET-123-ABC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.qrCode").value("TICKET-123-ABC"));

        verify(ticketService).getTicketByQrCode("TICKET-123-ABC");
    }

    @Test
    @WithMockUser(roles = "DRIVER")
    void getTicketsByTrip_ShouldReturnTripTickets() throws Exception {
        List<TicketResponse> tickets = Arrays.asList(ticketResponse);
        when(ticketService.getTicketsByTripId(1L)).thenReturn(tickets);

        mockMvc.perform(get("/api/v1/tickets/trip/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tripId").value(1));

        verify(ticketService).getTicketsByTripId(1L);
    }

    @Test
    @WithMockUser(roles = "DISPATCHER")
    void getTicketsByTripAndStatus_ShouldReturnFilteredTickets() throws Exception {
        List<TicketResponse> tickets = Arrays.asList(ticketResponse);
        when(ticketService.getTicketsByTripAndStatus(1L, TicketStatus.SOLD)).thenReturn(tickets);

        mockMvc.perform(get("/api/v1/tickets/trip/1/status/SOLD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("SOLD"));

        verify(ticketService).getTicketsByTripAndStatus(1L, TicketStatus.SOLD);
    }

    @Test
    @WithMockUser(roles = "CLERK")
    void getTicketsByPassenger_ShouldReturnPassengerTickets() throws Exception {
        List<TicketResponse> tickets = Arrays.asList(ticketResponse);
        when(ticketService.getTicketsByPassengerId(1L)).thenReturn(tickets);

        mockMvc.perform(get("/api/v1/tickets/passenger/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].passengerId").value(1));

        verify(ticketService).getTicketsByPassengerId(1L);
    }

    @Test
    @WithMockUser(roles = "CLERK")
    void updateTicket_ShouldUpdateAndReturnTicket() throws Exception {
        TicketUpdateRequest updateRequest = new TicketUpdateRequest(TicketStatus.USED);

        when(ticketService.updateTicket(eq(1L), any(TicketUpdateRequest.class)))
                .thenReturn(ticketResponse);

        mockMvc.perform(put("/api/v1/tickets/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());

        verify(ticketService).updateTicket(eq(1L), any(TicketUpdateRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteTicket_ShouldReturnNoContent() throws Exception {
        doNothing().when(ticketService).deleteTicket(1L);

        mockMvc.perform(delete("/api/v1/tickets/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(ticketService).deleteTicket(1L);
    }

    // ==================== DRIVER ENDPOINTS ====================

    @Test
    @WithMockUser(roles = "DRIVER")
    void markAsUsed_ShouldUpdateTicketStatus() throws Exception {
        TicketResponse usedResponse = new TicketResponse(
                1L, 1L, "2025-01-15", "08:00", 1L, "John Doe",
                1L, "Bogotá", 2L, "Tunja", "A12",
                new BigDecimal("45000"), "CASH", "USED",
                "TICKET-123-ABC", LocalDateTime.now(),null,BigDecimal.ZERO,
                CancellationPolicy.NO_REFUND,                "Bogotá",
                "Tunja",
                "xxx55"
        );
        when(ticketService.markAsUsed(1L)).thenReturn(usedResponse);

        mockMvc.perform(post("/api/v1/tickets/1/used")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("USED"));

        verify(ticketService).markAsUsed(1L);
    }

    @Test
    @WithMockUser(roles = "DRIVER")
    void markAsNoShow_ShouldUpdateTicketStatus() throws Exception {
        TicketResponse noShowResponse = new TicketResponse(
                1L, 1L, "2025-01-15", "08:00", 1L, "John Doe",
                1L, "Bogotá", 2L, "Tunja", "A12",
                new BigDecimal("45000"), "CASH", "NO_SHOW",
                "TICKET-123-ABC", LocalDateTime.now(),null,BigDecimal.ZERO,
                CancellationPolicy.NO_REFUND, "Bogotá",
                "Tunja",
                "xxx55"
        );
        when(ticketService.markAsNoShow(1L)).thenReturn(noShowResponse);

        mockMvc.perform(post("/api/v1/tickets/1/no-show")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("NO_SHOW"));

        verify(ticketService).markAsNoShow(1L);
    }

    // ==================== QUERIES ====================

    @Test
    @WithMockUser(roles = "CLERK")
    void isSeatAvailable_ShouldReturnAvailability() throws Exception {
        when(ticketService.isSeatAvailable(1L, "A12")).thenReturn(true);

        mockMvc.perform(get("/api/v1/tickets/check-availability")
                        .param("tripId", "1")
                        .param("seatNumber", "A12"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(ticketService).isSeatAvailable(1L, "A12");
    }

    @Test
    @WithMockUser(roles = "DISPATCHER")
    void countSoldTicketsByTrip_ShouldReturnCount() throws Exception {
        when(ticketService.countSoldTicketsByTrip(1L)).thenReturn(25L);

        mockMvc.perform(get("/api/v1/tickets/trip/1/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("25"));

        verify(ticketService).countSoldTicketsByTrip(1L);
    }

    // ==================== VALIDATION TESTS ====================

    @Test
    @WithMockUser(roles = "CLERK")
    void createTicket_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        TicketCreateRequest invalidRequest = new TicketCreateRequest(
                null,  // null tripId
                null,  // null passengerId
                null,  // null fromStopId
                null,  // null toStopId
                "",    // blank seatNumber
                null   // null paymentMethod
        );

        mockMvc.perform(post("/api/v1/tickets")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(ticketService, never()).createTicket(any());
    }

    // ==================== SECURITY TESTS ====================

    @Test
    void createTicket_WithoutAuth_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/tickets")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "PASSENGER")
    void getAllTickets_WithPassengerRole_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/tickets"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CLERK")
    void deleteTicket_WithClerkRole_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(delete("/api/v1/tickets/1")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }
}