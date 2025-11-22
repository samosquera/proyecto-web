package com.bers.api.controllers;

import com.bers.api.dtos.UserDtos.*;
import com.bers.domain.entities.enums.UserRole;
import com.bers.domain.entities.enums.UserStatus;
import com.bers.security.config.AuthService;
import com.bers.security.config.CustomUserDetailsService;
import com.bers.security.config.JwtAuthenticationFilter;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=none"
})
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("UserController - Unit Tests")
class UserControllerTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper om;
    @MockitoBean
    private UserService userService;
    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;
    @MockitoBean
    private AuthService authService;

    private UserCreateRequest createRequest;
    private UserUpdateRequest updateRequest;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        createRequest = new UserCreateRequest(
                "Juan Perez",
                "juan@example.com",
                "3001234567",
                UserRole.PASSENGER,
                "password123",
                LocalDate.of(2000,03,25)
        );

        updateRequest = new UserUpdateRequest(
                "Juan Updated",
                "3009876543",
                UserStatus.ACTIVE
        );

        userResponse = new UserResponse(
                1L,
                "Juan Perez",
                "juan@example.com",
                "3001234567",
                UserRole.PASSENGER.name(),
                UserStatus.ACTIVE.name(),
                LocalDate.of(2000,03,25),
                LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("POST /api/v1/users - crear usuario exitosamente (ADMIN)")
    @WithMockUser(roles = "ADMIN")
    void shouldCreateUserSuccessfully() throws Exception {
        when(userService.create(any(UserCreateRequest.class))).thenReturn(userResponse);

        mvc.perform(post("/api/v1/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("Juan Perez"))
                .andExpect(jsonPath("$.email").value("juan@example.com"));

        verify(userService).create(any(UserCreateRequest.class));
    }

    @Test
    @DisplayName("POST /api/v1/users - error validación email inválido (ADMIN)")
    @WithMockUser(roles = "ADMIN")
    void shouldThrowValidationErrorWhenEmailIsInvalid() throws Exception {
        UserCreateRequest invalidRequest = new UserCreateRequest(
                "Juan",
                "invalid-email",
                "3001234567",
                UserRole.PASSENGER,
                "password123",
                LocalDate.of(1995, 1, 1) // CORRECCIÓN: Agregar dateOfBirth
        );

        mvc.perform(post("/api/v1/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).create(any());
    }

    @Test
    @DisplayName("PUT /api/v1/users/{id} - actualizar usuario (ADMIN)")
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateUserSuccessfully() throws Exception {
        when(userService.update(anyLong(), any(UserUpdateRequest.class))).thenReturn(userResponse);

        mvc.perform(put("/api/v1/users/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(userService).update(anyLong(), any(UserUpdateRequest.class));
    }

    @Test
    @DisplayName("GET /api/v1/users/{id} - obtener usuario por ID (DISPATCHER)")
    @WithMockUser(roles = "DISPATCHER")
    void shouldGetUserById() throws Exception {
        when(userService.getById(anyLong())).thenReturn(userResponse);

        mvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("juan@example.com"));

        verify(userService).getById(anyLong());
    }

    @Test
    @DisplayName("GET /api/v1/users/email/{email} - obtener usuario por email (CLERK)")
    @WithMockUser(roles = "CLERK")
    void shouldGetUserByEmail() throws Exception {
        when(userService.getByEmail("juan@example.com")).thenReturn(userResponse);

        mvc.perform(get("/api/v1/users/email/juan@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("juan@example.com"));

        verify(userService).getByEmail("juan@example.com");
    }

    @Test
    @DisplayName("GET /api/v1/users/phone/{phone} - obtener usuario por teléfono (DISPATCHER)")
    @WithMockUser(roles = "DISPATCHER")
    void shouldGetUserByPhone() throws Exception {
        when(userService.getByPhone("3001234567")).thenReturn(userResponse);

        mvc.perform(get("/api/v1/users/phone/3001234567"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phone").value("3001234567"));

        verify(userService).getByPhone("3001234567");
    }

    @Test
    @DisplayName("GET /api/v1/users - listar todos (ADMIN)")
    @WithMockUser(roles = "ADMIN")
    void shouldGetAllUsers() throws Exception {
        UserResponse user2 = new UserResponse(2L, "Maria", "maria@example.com",
                "3001111111", UserRole.DRIVER.name(), UserStatus.ACTIVE.name(), LocalDate.of(1998, 10, 10), LocalDateTime.now());

        when(userService.getAll()).thenReturn(List.of(userResponse, user2));

        mvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(userService).getAll();
    }

    @Test
    @DisplayName("GET /api/v1/users/role/{role} - listar por rol (ADMIN)")
    @WithMockUser(roles = "ADMIN")
    void shouldGetUsersByRole() throws Exception {
        when(userService.getByRole(UserRole.PASSENGER)).thenReturn(List.of(userResponse));

        mvc.perform(get("/api/v1/users/role/PASSENGER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(userService).getByRole(UserRole.PASSENGER);
    }

    @Test
    @DisplayName("PATCH /api/v1/users/{id}/status - cambiar estado (ADMIN)")
    @WithMockUser(roles = "ADMIN")
    void shouldChangeUserStatus() throws Exception {
        UserResponse inactiveUser = new UserResponse(1L, "Juan Perez", "juan@example.com",
                "3001234567", UserRole.PASSENGER.name(), UserStatus.INACTIVE.name(), LocalDate.of(2000, 3, 25), LocalDateTime.now());

        when(userService.changeStatus(anyLong(), eq(UserStatus.INACTIVE))).thenReturn(inactiveUser);

        mvc.perform(patch("/api/v1/users/1/status")
                        .with(csrf())
                        .param("status", "INACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"));

        verify(userService).changeStatus(anyLong(), eq(UserStatus.INACTIVE));
    }

    @Test
    @DisplayName("DELETE /api/v1/users/{id} - eliminar usuario (ADMIN)")
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteUser() throws Exception {
        doNothing().when(userService).delete(anyLong());

        mvc.perform(delete("/api/v1/users/1").with(csrf()))
                .andExpect(status().isNoContent());

        verify(userService).delete(anyLong());
    }

    @Test
    @DisplayName("DELETE /api/v1/users/{id} - denegar acceso sin ADMIN")
    @WithMockUser(roles = "PASSENGER")
    void shouldDenyAccessWhenUserIsNotAdmin() throws Exception {
        mvc.perform(delete("/api/v1/users/1").with(csrf()))
                .andExpect(status().isForbidden());

        verify(userService, never()).delete(any());
    }

    @Test
    @DisplayName("GET /api/v1/users/exists/email/{email} - verificar email (ADMIN)")
    @WithMockUser(roles = "ADMIN")
    void shouldCheckIfEmailExists() throws Exception {
        when(userService.existsByEmail("juan@example.com")).thenReturn(true);

        mvc.perform(get("/api/v1/users/exists/email/juan@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(userService).existsByEmail("juan@example.com");
    }

    @Test
    @DisplayName("GET /api/v1/users/exists/phone/{phone} - verificar teléfono (ADMIN)")
    @WithMockUser(roles = "ADMIN")
    void shouldCheckIfPhoneExists() throws Exception {
        when(userService.existsByPhone("3001234567")).thenReturn(false);

        mvc.perform(get("/api/v1/users/exists/phone/3001234567"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        verify(userService).existsByPhone("3001234567");
    }

    @Test
    @DisplayName("POST /api/v1/users - phone inválido (ADMIN)")
    @WithMockUser(roles = "ADMIN")
    void shouldThrowErrorWhenPhoneFormatIsInvalid() throws Exception {
        UserCreateRequest invalidRequest = new UserCreateRequest(
                "Juan",
                "juan@example.com",
                "123",
                UserRole.PASSENGER,
                "password123",
                LocalDate.of(1995, 1, 1)
        );

        mvc.perform(post("/api/v1/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).create(any());
    }
}