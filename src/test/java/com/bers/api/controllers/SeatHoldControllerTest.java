package com.bers.api.controllers;

import com.bers.api.dtos.SeatHoldDtos.*;
import com.bers.security.config.JwtService;
import com.bers.services.service.SeatHoldService;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SeatHoldController.class)
class SeatHoldControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private SeatHoldService seatHoldService;
    @MockitoBean private JwtService jwtService;

    private SeatHoldResponse seatHoldResponse;

    @BeforeEach
    void setUp() {
        seatHoldResponse = new SeatHoldResponse(
                1L, "A12", LocalDateTime.now().plusMinutes(10), "HOLD",
                LocalDateTime.now(), 1L, 1L, "2025-01-15", "08:00",
                "Bogotá - Tunja", 10
        );
    }

    @Test
    @WithMockUser(username = "john@example.com", roles = "PASSENGER")
    void createSeatHold_ShouldCreateAndReturnSeatHold() throws Exception {
        SeatHoldCreateRequest createRequest = new SeatHoldCreateRequest(1L, "A12", 1L, 2L);
        when(seatHoldService.createSeatHold(any(SeatHoldCreateRequest.class), anyLong()))
                .thenReturn(seatHoldResponse);

        mockMvc.perform(post("/api/v1/seat-holds")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.seatNumber").value("A12"));

        verify(seatHoldService).createSeatHold(any(SeatHoldCreateRequest.class), anyLong());
    }

    @Test
    @WithMockUser(roles = "PASSENGER")
    void getMyHolds_ShouldReturnUserHolds() throws Exception {
        when(seatHoldService.getSeatHoldsByUserId(anyLong()))
                .thenReturn(Arrays.asList(seatHoldResponse));

        mockMvc.perform(get("/api/v1/seat-holds/my-holds"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].seatNumber").value("A12"));

        verify(seatHoldService).getSeatHoldsByUserId(anyLong());
    }

    @Test
    @WithMockUser(roles = "CLERK")
    void isSeatHeld_ShouldReturnBoolean() throws Exception {
        when(seatHoldService.isSeatHeld(1L, "A12")).thenReturn(true);

        mockMvc.perform(get("/api/v1/seat-holds/check")
                        .param("tripId", "1")
                        .param("seatNumber", "A12"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(seatHoldService).isSeatHeld(1L, "A12");
    }
}