package com.bers.api.controllers;

import com.bers.api.dtos.ConfigDtos.*;
import com.bers.security.config.JwtService;
import com.bers.services.service.ConfigService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ConfigController.class)
class ConfigControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private ConfigService configService;
    @MockitoBean private JwtService jwtService;

    private ConfigResponse configResponse;

    @BeforeEach
    void setUp() {
        configResponse = new ConfigResponse(
                1L, "seat.hold.minutes", "10", "Hold time in minutes", LocalDateTime.now()
        );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createConfig_ShouldCreateAndReturnConfig() throws Exception {
        ConfigCreateRequest createRequest = new ConfigCreateRequest(
                "seat.hold.minutes", "10", "Hold time"
        );
        when(configService.createConfig(any(ConfigCreateRequest.class))).thenReturn(configResponse);

        mockMvc.perform(post("/api/v1/configs")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.key").value("seat.hold.minutes"));

        verify(configService).createConfig(any(ConfigCreateRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllConfigs_ShouldReturnConfigList() throws Exception {
        when(configService.getAllConfigs()).thenReturn(Arrays.asList(configResponse));

        mockMvc.perform(get("/api/v1/configs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].key").value("seat.hold.minutes"));

        verify(configService).getAllConfigs();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getConfigByKey_ShouldReturnConfig() throws Exception {
        when(configService.getConfigByKey("seat.hold.minutes")).thenReturn(configResponse);

        mockMvc.perform(get("/api/v1/configs/key/seat.hold.minutes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value").value("10"));

        verify(configService).getConfigByKey("seat.hold.minutes");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getConfigValueAsInt_ShouldReturnIntValue() throws Exception {
        when(configService.getConfigValueAsInt("seat.hold.minutes", 10)).thenReturn(10);

        mockMvc.perform(get("/api/v1/configs/key/seat.hold.minutes/value/int")
                        .param("defaultValue", "10"))
                .andExpect(status().isOk())
                .andExpect(content().string("10"));

        verify(configService).getConfigValueAsInt("seat.hold.minutes", 10);
    }
}