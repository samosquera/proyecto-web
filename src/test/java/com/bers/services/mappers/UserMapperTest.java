package com.bers.services.mappers;

import com.bers.api.dtos.UserDtos.*;
import com.bers.domain.entities.User;
import com.bers.domain.entities.enums.UserRole;
import com.bers.domain.entities.enums.UserStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("UserMapper Tests")
class UserMapperTest {

    private UserMapper userMapper;

    @BeforeEach
    void setUp() {
        userMapper = Mappers.getMapper(UserMapper.class);
    }

    @Test
    @DisplayName("Debe mapear UserCreateRequest a la entidad User")
    void shouldMapCreateRequestToEntity() {
        UserCreateRequest request = new UserCreateRequest(
                "John Doe",
                "john.doe@example.com",
                "3001234567",
                UserRole.PASSENGER,
                "password123",
                LocalDate.of(2000,03,27)
        );
        User user = userMapper.toEntity(request);
        assertNotNull(user);
        Assertions.assertEquals("John Doe", user.getUsername());
        Assertions.assertEquals("john.doe@example.com", user.getEmail());
        Assertions.assertEquals("3001234567", user.getPhone());
        Assertions.assertEquals(UserRole.PASSENGER, user.getRole());
        assertNull(user.getId());
        assertNull(user.getPasswordHash());
        assertNotNull(user.getCreateAt());
    }

    @Test
    @DisplayName("Debe actualizar la entidad User desde UserUpdateRequest")
    void shouldUpdateEntityFromUpdateRequest() {
        User existingUser = User.builder()
                .id(1L)
                .username("Old Name")
                .email("old@example.com")
                .phone("3009999999")
                .role(UserRole.PASSENGER)
                .status(UserStatus.ACTIVE)
                .passwordHash("hashedpass")
                .createAt(LocalDateTime.now())
                .build();

        UserUpdateRequest request = new UserUpdateRequest(
                "New Name",
                "3001111111",
                UserStatus.INACTIVE
        );
        userMapper.updateEntity(request, existingUser);

        Assertions.assertEquals("New Name", existingUser.getUsername());
        Assertions.assertEquals("3001111111", existingUser.getPhone());
        Assertions.assertEquals(UserStatus.INACTIVE, existingUser.getStatus());

        Assertions.assertEquals("old@example.com", existingUser.getEmail());
        Assertions.assertEquals(UserRole.PASSENGER, existingUser.getRole());
    }

    @Test
    @DisplayName("Debe mapear la entidad User a UserResponse")
    void shouldMapEntityToResponse() {

        LocalDateTime now = LocalDateTime.now();
        User user = User.builder()
                .id(1L)
                .username("John Doe")
                .email("john.doe@example.com")
                .phone("3001234567")
                .role(UserRole.CLERK)
                .status(UserStatus.ACTIVE)
                .passwordHash("hashedpass")
                .createAt(now)
                .build();

        UserResponse response = userMapper.toResponse(user);


        assertNotNull(response);
        Assertions.assertEquals(1L, response.id());
        Assertions.assertEquals("John Doe", response.username());
        Assertions.assertEquals("john.doe@example.com", response.email());
        Assertions.assertEquals("3001234567", response.phone());
        Assertions.assertEquals("CLERK", response.role());
        Assertions.assertEquals("ACTIVE", response.status());
        Assertions.assertEquals(now, response.createAt());
    }

    @Test
    @DisplayName("Debe manejar valores nulos en UserCreateRequest")
    void shouldHandleNullValuesInCreateRequest() {
        // Given
        UserCreateRequest request = new UserCreateRequest(
                null,
                null,
                null,
                null,
                null,
                null
        );

        User user = userMapper.toEntity(request);

        assertNotNull(user);
        assertNull(user.getUsername());
        assertNull(user.getEmail());
        assertNull(user.getPhone());
        assertNull(user.getRole());
    }

    @Test
    @DisplayName("Debe mapear todos los tipos de UserRole correctamente")
    void shouldMapAllUserRoles() {
        for (UserRole role : UserRole.values()) {
            UserCreateRequest request = new UserCreateRequest(
                    "Test User",
                    "test@example.com",
                    "3001234567",
                    role,
                    "password",
                    LocalDate.of(2000,03,27)
            );

            User user = userMapper.toEntity(request);
            Assertions.assertEquals(role, user.getRole());

            user.setId(1L);
            user.setCreateAt(LocalDateTime.now());
            user.setPasswordHash("hash");

            UserResponse response = userMapper.toResponse(user);
            Assertions.assertEquals(role.name(), response.role());
        }
    }

    @Test
    @DisplayName("Debe mapear todos los tipos de UserStatus correctamente")
    void shouldMapAllUserStatuses() {
        for (UserStatus status : UserStatus.values()) {
            User user = User.builder()
                    .id(1L)
                    .username("Test")
                    .email("test@example.com")
                    .phone("3001234567")
                    .role(UserRole.PASSENGER)
                    .status(status)
                    .passwordHash("hash")
                    .createAt(LocalDateTime.now())
                    .build();

            UserResponse response = userMapper.toResponse(user);
            Assertions.assertEquals(status.name(), response.status());
        }
    }
}