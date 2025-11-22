package com.bers.api.controllers;

import com.bers.api.dtos.ParcelDtos.*;
import com.bers.domain.entities.enums.ParcelStatus;
import com.bers.security.config.JwtService;
import com.bers.services.service.ParcelService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ParcelController.class)
class ParcelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ParcelService parcelService;

    @MockitoBean
    private JwtService jwtService;

    private ParcelResponse parcelResponse;
    private ParcelCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        parcelResponse = new ParcelResponse(
                1L,
                "PCL-12345",
                "Juan Sender",
                "3001234567",
                "Maria Receiver",
                "3009876543",
                new BigDecimal("15000"),
                "CREATED",
                null,
                "123456",
                LocalDateTime.now(),
                null,
                1L,
                2L,
                1L
        );

        createRequest = new ParcelCreateRequest(
                "Juan Sender",
                "3001234567",
                "Maria Receiver",
                "3009876543",
                new BigDecimal("15000"),
                1L,
                2L,
                1L
        );
    }

    @Test
    @WithMockUser(roles = "CLERK")
    void createParcel_ShouldCreateAndReturnParcel() throws Exception {
        when(parcelService.createParcel(any(ParcelCreateRequest.class))).thenReturn(parcelResponse);

        mockMvc.perform(post("/api/v1/parcels")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("PCL-12345"));

        verify(parcelService).createParcel(any(ParcelCreateRequest.class));
    }

    @Test
    @WithMockUser(roles = "CLERK")
    void getAllParcels_ShouldReturnParcelList() throws Exception {
        List<ParcelResponse> parcels = Arrays.asList(parcelResponse);
        when(parcelService.getAllParcels()).thenReturn(parcels);

        mockMvc.perform(get("/api/v1/parcels"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(parcelService).getAllParcels();
    }

    @Test
    @WithMockUser(roles = "DRIVER")
    void getParcelByCode_ShouldReturnParcel() throws Exception {
        when(parcelService.getParcelByCode("PCL-12345")).thenReturn(parcelResponse);

        mockMvc.perform(get("/api/v1/parcels/code/PCL-12345"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("PCL-12345"));

        verify(parcelService).getParcelByCode("PCL-12345");
    }

    @Test
    @WithMockUser(roles = "CLERK")
    void markAsInTransit_ShouldUpdateParcelStatus() throws Exception {
        ParcelResponse inTransitResponse = new ParcelResponse(
                1L, "PCL-12345", "Juan Sender", "3001234567",
                "Maria Receiver", "3009876543", new BigDecimal("15000"),
                "IN_TRANSIT", null, "123456", LocalDateTime.now(),
                null, 1L, 2L, 1L
        );
        when(parcelService.markAsInTransit(1L, 1L)).thenReturn(inTransitResponse);

        mockMvc.perform(post("/api/v1/parcels/1/in-transit")
                        .with(csrf())
                        .param("tripId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_TRANSIT"));

        verify(parcelService).markAsInTransit(1L, 1L);
    }

    @Test
    @WithMockUser(roles = "DRIVER")
    void markAsDelivered_WithValidOtp_ShouldUpdateParcelStatus() throws Exception {
        ParcelResponse deliveredResponse = new ParcelResponse(
                1L, "PCL-12345", "Juan Sender", "3001234567",
                "Maria Receiver", "3009876543", new BigDecimal("15000"),
                "DELIVERED", "http://photo.url", "123456",
                LocalDateTime.now(), LocalDateTime.now(), 1L, 2L, 1L
        );
        when(parcelService.markAsDelivered(1L, "123456", "http://photo.url",1l))
                .thenReturn(deliveredResponse);

        mockMvc.perform(post("/api/v1/parcels/1/delivered")
                        .with(csrf())
                        .param("otp", "123456")
                        .param("photoUrl", "http://photo.url"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DELIVERED"));

        verify(parcelService).markAsDelivered(1L, "123456", "http://photo.url",1L);
    }

    @Test
    @WithMockUser(roles = "CLERK")
    void markAsFailed_ShouldUpdateParcelStatus() throws Exception {
        ParcelResponse failedResponse = new ParcelResponse(
                1L, "PCL-12345", "Juan Sender", "3001234567",
                "Maria Receiver", "3009876543", new BigDecimal("15000"),
                "FAILED", null, "123456", LocalDateTime.now(),
                null, 1L, 2L, 1L
        );
        when(parcelService.markAsFailed(1L, "Receiver not found"))
                .thenReturn(failedResponse);

        mockMvc.perform(post("/api/v1/parcels/1/failed")
                        .with(csrf())
                        .param("reason", "Receiver not found"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FAILED"));

        verify(parcelService).markAsFailed(1L, "Receiver not found");
    }

    @Test
    @WithMockUser(roles = "CLERK")
    void getParcelsByStatus_ShouldReturnFilteredParcels() throws Exception {
        List<ParcelResponse> parcels = Arrays.asList(parcelResponse);
        when(parcelService.getParcelsByStatus(ParcelStatus.CREATED)).thenReturn(parcels);

        mockMvc.perform(get("/api/v1/parcels/status/CREATED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("CREATED"));

        verify(parcelService).getParcelsByStatus(ParcelStatus.CREATED);
    }

    @Test
    @WithMockUser(roles = "DRIVER")
    void getParcelsByTrip_ShouldReturnTripParcels() throws Exception {
        List<ParcelResponse> parcels = Arrays.asList(parcelResponse);
        when(parcelService.getParcelsByTripId(1L)).thenReturn(parcels);

        mockMvc.perform(get("/api/v1/parcels/trip/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tripId").value(1));

        verify(parcelService).getParcelsByTripId(1L);
    }

    @Test
    @WithMockUser(roles = "CLERK")
    void validateOtp_WithCorrectOtp_ShouldReturnTrue() throws Exception {
        when(parcelService.validateOtp(1L, "123456")).thenReturn(true);

        mockMvc.perform(post("/api/v1/parcels/1/validate-otp")
                        .with(csrf())
                        .param("otp", "123456"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(parcelService).validateOtp(1L, "123456");
    }

    @Test
    @WithMockUser(roles = "PASSENGER")
    void createParcel_WithPassengerRole_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(post("/api/v1/parcels")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());
    }
}