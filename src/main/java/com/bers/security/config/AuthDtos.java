package com.bers.security.config;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;

import java.io.Serializable;
import java.time.LocalDate;


public class AuthDtos {

    public record RegisterRequest(
            @NotBlank(message = "Username is required")
            @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
            String username,

            @NotBlank(message = "Email is required")
            @Email(message = "Email must be valid")
            String email,

            @NotBlank(message = "Phone is required")
            @Pattern(regexp = "\\d{10}", message = "Phone must be exactly 10 digits")
            String phone,

            @NotBlank(message = "Password is required")
            @Size(min = 8, message = "Password must be at least 8 characters")
            @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$",
                    message = "Password must contain at least one letter and one number")
            String password,

            @JsonFormat(pattern = "yyyy-MM-dd")
            @NotNull(message = "La fecha de nacimiento es obligatoria")
            LocalDate dateOfBirth

    ) implements Serializable {
    }

    /**
     * Login de usuario con email y contraseña.
     */
    public record LoginRequest(
            @NotBlank(message = "Email is required")
            @Email(message = "Email must be valid")
            String email,

            @NotBlank(message = "Password is required")
            String password
    ) implements Serializable {
    }

    /**
     * Login con teléfono.
     */
    public record PhoneLoginRequest(
            @NotBlank(message = "Phone is required")
            @Pattern(regexp = "\\d{10}", message = "Phone must be exactly 10 digits")
            String phone,

            @NotBlank(message = "Password is required")
            String password
    ) implements Serializable {
    }

    /**
     * DTO con la información básica del usuario autenticado.
     */
    public record UserInfo(
            Long id,
            String username,
            String email,
            String phone,
            String status
    ) implements Serializable {
    }

    /**
     * Respuesta de autenticación (login/register/refresh).
     */
    public record AuthResponse(
            String accessToken,
            String refreshToken,
            String tokenType,
            Long expiresIn,
            UserInfo user
    ) implements Serializable {
        public AuthResponse(String accessToken, String refreshToken, Long expiresIn, UserInfo user) {
            this(accessToken, refreshToken, "Bearer", expiresIn, user);
        }

        //Constructor para token offline
        public static AuthResponse forOffline(String offlineToken, Long expiresIn, UserInfo user) {
            return new AuthResponse(offlineToken, null, "Bearer", expiresIn, user);
        }
    }

    /**
     * Solicitud de refresh de token.
     */
    public record RefreshTokenRequest(
            @NotBlank(message = "Refresh token is required")
            String refreshToken
    ) implements Serializable {
    }

    /**
     * Cambio de contraseña (usuario autenticado).
     */
    public record ChangePasswordRequest(
            @NotBlank(message = "Current password is required")
            String currentPassword,

            @NotBlank(message = "New password is required")
            @Size(min = 8, message = "New password must be at least 8 characters")
            @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$",
                    message = "Password must contain at least one letter and one number")
            String newPassword,

            @NotBlank(message = "Confirmation password is required")
            String confirmPassword
    ) implements Serializable {
    }

    /**
     * Solicitud de recuperación de contraseña (enviar token al email).
     */
    public record ForgotPasswordRequest(
            @NotBlank(message = "Email is required")
            @Email(message = "Email must be valid")
            String email
    ) implements Serializable {
    }

    public record ResetPasswordRequest(
            @NotBlank(message = "Reset token is required")
            String token,

            @NotBlank(message = "New password is required")
            @Size(min = 8, message = "Password must be at least 8 characters")
            @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$",
                    message = "Password must contain at least one letter and one number")
            String newPassword,

            @NotBlank(message = "Confirmation password is required")
            String confirmPassword
    ) implements Serializable {
    }


    public record MessageResponse(
            String message,
            boolean success
    ) implements Serializable {
        public MessageResponse(String message) {
            this(message, true);
        }
    }

    public record LogoutRequest(
            @NotBlank(message = "Access token is required")
            String accessToken,

            String refreshToken
    ) implements Serializable {
    }

    public record TokenValidationResponse(
            boolean valid,
            String username,
            String role,
            Long userId,
            Long expiresIn  // Segundos restantes
    ) implements Serializable {
    }
}