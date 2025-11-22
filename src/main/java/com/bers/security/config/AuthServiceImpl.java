package com.bers.security.config;

import com.bers.domain.entities.User;
import com.bers.domain.entities.enums.UserRole;
import com.bers.domain.entities.enums.UserStatus;
import com.bers.domain.repositories.UserRepository;
import com.bers.security.config.AuthDtos.*;
import com.bers.security.config.domine.entities.PasswordResetToken;
import com.bers.security.config.domine.repository.PasswordResetTokenRepository;
import com.bers.security.config.service.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;


    // Constantes para configuracion
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Attempting to register new user with email: {}", request.email());

        // Validaciones mejoradas
        validateRegistrationRequest(request);

        // Ignorar el role del request y SIEMPRE crear como PASSENGER
        UserRole assignedRole = UserRole.PASSENGER;

        User user = User.builder()
                .username(request.username().trim())
                .email(request.email().toLowerCase().trim())
                .phone(request.phone().trim())
                .role(assignedRole)
                .status(UserStatus.ACTIVE)
                .passwordHash(passwordEncoder.encode(request.password()))
                .dateOfBirth(request.dateOfBirth())
                .createAt(LocalDateTime.now())
                .build();

        user = userRepository.save(user);
        log.info("New user registered successfully: {} [{}]", user.getEmail(), user.getRole());

        //  Generar tokens JWT automaticamente (auto-login)
        UserDetails userDetails = new CustomUserDetails(user);
        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        // Podria incluirse correo de bienvenida
        return new AuthResponse(
                accessToken,
                refreshToken,
                jwtService.getExpirationTime(),
                mapToUserInfo(user)
        );
    }

    // Validaciones de registro centralizados
    private void validateRegistrationRequest(RegisterRequest request) {
        // Validar email único
        if (userRepository.existsByEmail(request.email())) {
            log.warn("Registration failed: Email already exists: {}", request.email());
            throw new IllegalArgumentException("Email already registered");
        }

        // Validar telefono unico
        if (userRepository.existsByPhone(request.phone())) {
            log.warn("Registration failed: Phone already exists: {}", request.phone());
            throw new IllegalArgumentException("Phone already registered");
        }

        // Validar username
        if (request.username().trim().length() < 3) {
            throw new IllegalArgumentException("Username must be at least 3 characters");
        }

        // Validar formato de email
        if (!isValidEmail(request.email())) {
            throw new IllegalArgumentException("Invalid email format");
        }

        // Validar fortaleza de contraseña
        if (isPasswordStrong(request.password())) {
            throw new IllegalArgumentException("Password must be at least 8 characters with letters and numbers");
        }
    }

    // Login (email + pass)
    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.email());

        try {
            //  Autenticar usando Spring Security
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email().toLowerCase().trim(),
                            request.password()
                    )
            );
        } catch (DisabledException e) {
            log.warn("Login failed: Account disabled for email: {}", request.email());
            throw new IllegalStateException("Account is disabled or inactive");
        } catch (BadCredentialsException e) {
            log.warn("Login failed: Invalid credentials for email: {}", request.email());
            throw new BadCredentialsException("Invalid email or password");
        }

        //  Obtener usuario de la BD
        User user = userRepository.findByEmail(request.email().toLowerCase().trim())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        //  Validar estado de la cuenta
        validateUserStatus(user);

        //  Generar tokens
        UserDetails userDetails = new CustomUserDetails(user);
        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        log.info("User logged in successfully: {} [{}]", user.getEmail(), user.getRole());

        return new AuthResponse(
                accessToken,
                refreshToken,
                jwtService.getExpirationTime(),
                mapToUserInfo(user)
        );
    }

    // Refrescar el token de acceso
    @Override
    @Transactional(readOnly = true)
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        final String refreshToken = request.refreshToken();

        try {
            //  Extraer email del refresh token
            final String userEmail = jwtService.extractUsername(refreshToken);

            if (userEmail == null || userEmail.isBlank()) {
                log.warn("Token refresh failed: Invalid refresh token");
                throw new IllegalArgumentException("Invalid refresh token");
            }

            // Obtener usuario
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            // Validar que la cuenta este activa
            validateUserStatus(user);

            UserDetails userDetails = new CustomUserDetails(user);

            // Validar que el refresh token sea válido
            if (!jwtService.isRefreshTokenValid(refreshToken, userDetails)) {
                log.warn("Token refresh failed: Invalid or expired refresh token for user: {}", userEmail);
                throw new IllegalArgumentException("Invalid or expired refresh token");
            }

            // Generar nuevos tokens
            String newAccessToken = jwtService.generateToken(userDetails);
            String newRefreshToken = jwtService.generateRefreshToken(userDetails);

            log.info("Tokens refreshed successfully for user: {}", userEmail);

            return new AuthResponse(
                    newAccessToken,
                    newRefreshToken,
                    jwtService.getExpirationTime(),
                    mapToUserInfo(user)
            );

        } catch (Exception e) {
            log.error("Token refresh error: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid refresh token");
        }
    }

    // Cambiar contraseña de usuario autenticado
    @Override
    @Transactional
    public MessageResponse changePassword(String userEmail, ChangePasswordRequest request) {
        log.info("Password change requested for user: {}", userEmail);

        // Obtener usuario
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        //  Validar contraseña actual
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            log.warn("Password change failed: Incorrect current password for user: {}", userEmail);
            throw new BadCredentialsException("Current password is incorrect");
        }

        // Validaciones de nueva contraseña
        validateNewPassword(request, user);

        // Actualizar contraseña
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        log.info("Password changed successfully for user: {}", userEmail);

        // TODO: Enviar email de notificación
        // emailService.sendPasswordChangedNotification(user.getEmail(), user.getUsername());

        return new MessageResponse("Password changed successfully", true);
    }

    // Inicia proceso de recuperacion de contraseña
    @Override
    @Transactional
    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        log.info("Password reset requested for email: {}", request.email());

        // Buscar el usuario (no revelar si no existe)
        User user = userRepository.findByEmail(request.email().toLowerCase().trim()).orElse(null);

        if (user != null && user.getStatus() == UserStatus.ACTIVE) {
            // Generar token unico y fecha de expiracion
            String resetToken = UUID.randomUUID().toString();
            LocalDateTime expiryTime = LocalDateTime.now().plusHours(1);

            // Guardar el token en BD
            PasswordResetToken tokenEntity = PasswordResetToken.builder()
                    .user(user)
                    .token(resetToken)
                    .expiresAt(expiryTime)
                    .used(false)
                    .build();
            passwordResetTokenRepository.save(tokenEntity);
        } else {
            log.warn("Password reset requested for non-existent or inactive email: {}", request.email());
        }

        return new MessageResponse(
                "If your email is registered, you will receive password reset instructions shortly",
                true
        );
    }


    // Resetear contraseña con token de recuperacion
    @Override
    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        log.info("Password reset attempt with token: {}", request.token().substring(0, 8) + "...");

        // Buscar el token
        PasswordResetToken tokenEntity = passwordResetTokenRepository.findByToken(request.token())
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired reset token"));

        // Validar expiración y uso
        if (tokenEntity.isUsed() || tokenEntity.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Reset token is invalid or expired");
        }

        // Obtener usuario asociado
        User user = tokenEntity.getUser();

        // Validar coincidencia de contraseñas
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        // Validar fortaleza de contraseña
        if (isPasswordStrong(request.newPassword())) {
            throw new IllegalArgumentException("Password must contain letters and numbers");
        }

        // Actualizar contraseña
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        // Marcar token como usado
        tokenEntity.setUsed(true);
        passwordResetTokenRepository.save(tokenEntity);

        log.info("Password reset successful for user: {}", user.getEmail());
        // TODO: emailService.sendPasswordChangedNotification(user.getEmail(), user.getUsername());

        return new MessageResponse("Password reset successfully. You can now login with your new password.", true);
    }


    // Cerrar sesion (invalidar tokens)
    @Override
    @Transactional
    public MessageResponse logout(LogoutRequest request) {
        log.info("Logout requested");

        // Extraer expiracion del token
        Long expiresIn = jwtService.getTimeUntilExpiration(request.accessToken());
        tokenBlacklistService.blacklistToken(request.accessToken(), expiresIn * 1000);

        log.info("User logged out, token blacklisted for {} seconds", expiresIn);
        return new MessageResponse("Logged out successfully", true);
    }

    // Validar si un token es valido
    @Override
    @Transactional(readOnly = true)
    public TokenValidationResponse validateToken(String token) {
        try {
            String username = jwtService.extractUsername(token);

            if (username == null) {
                return new TokenValidationResponse(false, null, null, null, null);
            }

            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            boolean isValid = jwtService.isTokenValid(token, userDetails);

            if (!isValid) {
                return new TokenValidationResponse(false, null, null, null, null);
            }

            // Calcular tiempo restante
            Long expiresIn = jwtService.getTimeUntilExpiration(token);

            CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;

            return new TokenValidationResponse(
                    true,
                    username,
                    customUserDetails.getRole().name(),
                    customUserDetails.getId(),
                    expiresIn
            );

        } catch (Exception e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return new TokenValidationResponse(false, null, null, null, null);
        }
    }

    // Metodos de utilidad
    private void validateUserStatus(User user) {
        if (user.getStatus() != UserStatus.ACTIVE) {
            String message = switch (user.getStatus()) {
                case INACTIVE -> "Account is inactive. Please contact support.";
                case BLOCKED -> "Account is blocked. Please contact administrator.";
                default -> "Account is not active";
            };
            log.warn("Login failed: Account status is {} for user: {}", user.getStatus(), user.getEmail());
            throw new IllegalStateException(message);
        }
    }

    private void validateNewPassword(ChangePasswordRequest request, User user) {
        // Validar que las nuevas contraseñas coincidan
        if (!request.newPassword().equals(request.confirmPassword())) {
            log.warn("Password change failed: Passwords do not match for user: {}", user.getEmail());
            throw new IllegalArgumentException("New password and confirmation do not match");
        }

        // Validar que la nueva contraseña sea diferente de la actual
        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            log.warn("Password change failed: New password is same as current for user: {}", user.getEmail());
            throw new IllegalArgumentException("New password must be different from current password");
        }

        // Validar longitud mínima
        if (request.newPassword().length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }

        // Validar fortaleza
        if (isPasswordStrong(request.newPassword())) {
            throw new IllegalArgumentException("Password must contain letters and numbers");
        }
    }

    private boolean isPasswordStrong(String password) {
        return password.length() < 8 ||
                !password.matches(".*[A-Za-z].*") ||
                !password.matches(".*\\d.*");
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }


    // Convierte User a UserInfo DTO
    private UserInfo mapToUserInfo(User user) {
        return new UserInfo(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPhone(),
                user.getStatus().name()
        );
    }

    // Enmascarar telefono para logs
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 6) {
            return "***";
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 3);
    }
}