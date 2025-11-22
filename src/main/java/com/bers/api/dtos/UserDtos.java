package com.bers.api.dtos;

import com.bers.domain.entities.enums.UserRole;
import com.bers.domain.entities.enums.UserStatus;
import jakarta.validation.constraints.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class UserDtos {
    public record UserCreateRequest(
            @NotBlank(message = "username is required")
            String username,
            @NotBlank(message = "email is required")
            @Email(message = "email must be valid")
            String email,
            @NotBlank(message = "phone is required")
            @Pattern(regexp = "\\d{10}", message = "phone length must be 10")
            String phone,
            @NotNull(message = "role is required")
            UserRole role,
            @NotBlank(message = "password is required")
            @Size(min = 8, message = "password must be at least 8 characters")
            String password,
            @NotNull(message = "La fecha de nacimiento no puede ser nula")
            @Past(message = "Fecha debe ser pasada")
            LocalDate dateOfBirth
    ) implements Serializable {
    }

    public record UserUpdateRequest(
            String username,
            @Pattern(regexp = "\\d{10}")
            String phone,
            UserStatus status
    ) implements Serializable {
    }

    public record UserSelfUpdateRequest(
            @NotBlank(message = "username is required")
            String username,

            @NotBlank(message = "phone is required")
            @Pattern(regexp = "\\d{10}")
            String phone,

            @Email(message = "email must be valid")
            String email
    ) implements Serializable {
    }

    public record UserCheckRequest(
            @Email String email,
            @Pattern(regexp = "\\d{10}") String phone
    ) implements Serializable {
    }

    public record UserAvailabilityResponse(
            boolean emailAvailable,
            boolean phoneAvailable
    ) implements Serializable {
    }

    public record UserResponse(
            Long id,
            String username,
            String email,
            String phone,
            String role,
            String status,
            LocalDate dateOfBirth,
            LocalDateTime createAt
    ) implements Serializable {
    }
}
