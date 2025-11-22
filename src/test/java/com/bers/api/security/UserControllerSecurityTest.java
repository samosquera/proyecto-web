package com.bers.api.security;

import com.bers.api.dtos.UserDtos.*;
import com.bers.domain.entities.enums.UserRole;
import com.bers.domain.entities.enums.UserStatus;
import com.bers.security.config.CustomUserDetails;
import com.bers.security.config.CustomUserDetailsService;
import com.bers.security.config.JwtService;
import com.bers.services.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.jpa.hibernate.ddl-auto=none"
})
@AutoConfigureMockMvc
@DisplayName("🔐 UserController - Pruebas de seguridad con JWT")
class UserControllerSecurityTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private UserService userService;

    // 👇 importante: evitar que JwtFilter consulte la BD
    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    private String adminToken;
    private String passengerToken;

    private UserCreateRequest createRequest;
    private UserResponse userResponse;

    @BeforeEach
    void setup() {
        // === 1️⃣ Usuarios simulados para tokens ===
        CustomUserDetails adminUser = CustomUserDetails.builder()
                .id(1L)
                .userEmail("admin@example.com")
                .password("password")
                .role(UserRole.ADMIN)
                .status(UserStatus.ACTIVE)
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .build();

        CustomUserDetails passengerUser = CustomUserDetails.builder()
                .id(2L)
                .userEmail("passenger@example.com")
                .password("password")
                .role(UserRole.PASSENGER)
                .status(UserStatus.ACTIVE)
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_PASSENGER")))
                .build();

        // === 2️⃣ Mockear el CustomUserDetailsService ===
        when(userDetailsService.loadUserByUsername("admin@example.com"))
                .thenReturn(adminUser);
        when(userDetailsService.loadUserByUsername("passenger@example.com"))
                .thenReturn(passengerUser);

        // === 3️⃣ Generar JWTs válidos ===
        adminToken = jwtService.generateToken(adminUser);
        passengerToken = jwtService.generateToken(passengerUser);

        // === 4️⃣ Datos base ===
        createRequest = new UserCreateRequest(
                "Juan Perez",
                "juan@example.com",
                "3001234567",
                UserRole.PASSENGER,
                "password123",
                LocalDate.of(2000,03,27)
        );

        userResponse = new UserResponse(
                1L,
                "Juan Perez",
                "juan@example.com",
                "3001234567",
                UserRole.PASSENGER.name(),
                UserStatus.ACTIVE.name(),
                LocalDate.of(2000,03,27),
                LocalDateTime.now()
        );
    }

    // ✅ ADMIN puede crear usuario
    @Test
    @DisplayName("POST /api/v1/users → ADMIN puede crear usuario (201)")
    void adminCanCreateUser() throws Exception {
        when(userService.create(any(UserCreateRequest.class))).thenReturn(userResponse);

        mvc.perform(post("/api/v1/users")
                        .with(csrf())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("Juan Perez"));

        verify(userService).create(any(UserCreateRequest.class));
    }

    // 🚫 PASSENGER no puede crear usuario
    @Test
    @DisplayName("POST /api/v1/users → PASSENGER no puede crear usuario (403)")
    void passengerCannotCreateUser() throws Exception {
        mvc.perform(post("/api/v1/users")
                        .with(csrf())
                        .header("Authorization", "Bearer " + passengerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());

        verify(userService, never()).create(any());
    }

    // ❌ Sin token
    @Test
    @DisplayName("POST /api/v1/users → sin token → 401 Unauthorized")
    void shouldReturn401WithoutToken() throws Exception {
        mvc.perform(post("/api/v1/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(createRequest)))
                .andExpect(status().isUnauthorized());

        verify(userService, never()).create(any());
    }

    // 🧩 Token inválido
    @Test
    @DisplayName("POST /api/v1/users → token inválido → 401 Unauthorized")
    void shouldReturn401ForInvalidToken() throws Exception {
        mvc.perform(post("/api/v1/users")
                        .with(csrf())
                        .header("Authorization", "Bearer invalid.token.value")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(createRequest)))
                .andExpect(status().isUnauthorized());

        verify(userService, never()).create(any());
    }
}
