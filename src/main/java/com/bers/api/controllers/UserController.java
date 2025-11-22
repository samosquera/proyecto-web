package com.bers.api.controllers;

import com.bers.api.dtos.UserDtos.*;
import com.bers.domain.entities.enums.UserRole;
import com.bers.domain.entities.enums.UserStatus;
import com.bers.security.config.AuthDtos.ChangePasswordRequest;
import com.bers.security.config.AuthDtos.MessageResponse;
import com.bers.security.config.AuthService;
import com.bers.security.config.CustomUserDetails;
import com.bers.services.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Validated
@Slf4j
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    // Obtener datos del usuario logueado
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> getCurrentUser(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.debug("Getting current user profile for: {}", userDetails.getUsername());
        UserResponse response = userService.getById(userDetails.getId());
        return ResponseEntity.ok(response);
    }

    // Actualizar perfil del usuario logueado
    @PutMapping("/update-me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> updateCurrentUser(
            Authentication authentication,
            @Valid @RequestBody UserSelfUpdateRequest request
    ) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getId();

        log.info("User {} updating their own profile", userId);

        UserResponse updated = userService.updateSelf(userId, request);
        return ResponseEntity.ok(updated);
    }

    // Cambiar contraseña del usuario logueado
    @PatchMapping("/me/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> changeOwnPassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("User {} changing password", userDetails.getUsername());
        MessageResponse response = authService.changePassword(userDetails.getUsername(), request);
        return ResponseEntity.ok(response);
    }

    //Crear usuario -- Solo Admin
    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserCreateRequest request) {
        log.info("Admin creating new user: {}", request.role());
        UserResponse response = userService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Actualizar usuario -- Admin o Dispatcher
    @PutMapping("/update/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request) {
        log.info("Updating user ID: {}", id);
        UserResponse response = userService.update(id, request);
        return ResponseEntity.ok(response);
    }

    // Obtener usuario por id
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DISPATCHER') or #id == authentication.principal.id")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        log.debug("Getting user by ID: {}", id);
        UserResponse response = userService.getById(id);
        return ResponseEntity.ok(response);
    }

    // Obtener usuario por email
    @GetMapping("/email/{email}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<UserResponse> getUserByEmail(@PathVariable String email) {
        log.debug("Getting user by email: {}", email);
        UserResponse response = userService.getByEmail(email);
        return ResponseEntity.ok(response);
    }


    // Obtener usuario por telefono - SOLO ADMIN y DISPATCHER

    @GetMapping("/phone/{phone}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<UserResponse> getUserByPhone(@PathVariable String phone) {
        log.debug("Getting user by phone: {}", phone);
        UserResponse response = userService.getByPhone(phone);
        return ResponseEntity.ok(response);
    }


    // Obtener todos los usuarios - SOLO ADMIN y DISPATCHER

    @GetMapping("/all-users")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        log.debug("Getting all users");
        List<UserResponse> response = userService.getAll();
        return ResponseEntity.ok(response);
    }

    // Obtener usuarios por rol - SOLO ADMIN y DISPATCHER

    @GetMapping("/role/{role}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<List<UserResponse>> getUsersByRole(@PathVariable UserRole role) {
        log.debug("Getting users by role: {}", role);
        List<UserResponse> response = userService.getByRole(role);
        return ResponseEntity.ok(response);
    }


    // Obtener usuarios por rol y estado - SOLO ADMIN y DISPATCHER

    @GetMapping("/role/{role}/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<List<UserResponse>> getUsersByRoleAndStatus(
            @PathVariable UserRole role,
            @PathVariable UserStatus status) {
        log.debug("Getting users by role: {} and status: {}", role, status);
        List<UserResponse> response = userService.getByRoleAndStatus(role, status);
        return ResponseEntity.ok(response);
    }

    // Eliminar usuario - SOLO ADMIN
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("Admin deleting user: {}", id);
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // Cambiar estado de usuario - ADMIN y DISPATCHER

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<UserResponse> changeUserStatus(
            @PathVariable Long id,
            @RequestParam UserStatus status) {
        log.info("Changing status for user ID: {} to {}", id, status);
        UserResponse response = userService.changeStatus(id, status);
        return ResponseEntity.ok(response);
    }

    // Verificar si email existe (existe) -> ya se encuentra vinculado a un usuario

    @GetMapping("/exists/email/{email}")
    public ResponseEntity<Boolean> existsByEmail(@PathVariable String email) {
        log.debug("Checking if email exists: {}", email);
        boolean exists = userService.existsByEmail(email);
        return ResponseEntity.ok(exists);
    }

// Verificar si telefono existe (existe) -> ya se encuentra vinculado a un usuario

    @GetMapping("/exists/phone/{phone}")
    public ResponseEntity<Boolean> existsByPhone(@PathVariable String phone) {
        log.debug("Checking if phone exists: {}", phone);
        boolean exists = userService.existsByPhone(phone);
        return ResponseEntity.ok(exists);
    }

// Verificar disponibilidad

    @PostMapping("/check")
    public ResponseEntity<UserAvailabilityResponse> checkAvailability(
            @Valid @RequestBody UserCheckRequest request) {
        log.debug("Checking availability for email: {} and phone: {}",
                request.email(), request.phone());

        boolean emailAvailable = !userService.existsByEmail(request.email());
        boolean phoneAvailable = !userService.existsByPhone(request.phone());

        UserAvailabilityResponse response = new UserAvailabilityResponse(emailAvailable, phoneAvailable);
        return ResponseEntity.ok(response);
    }

    // Obtener usuarios activos por rol
    @GetMapping("/role/{role}/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<List<UserResponse>> getActiveUsersByRole(@PathVariable UserRole role) {
        log.debug("Getting active users by role: {}", role);
        List<UserResponse> response = userService.getByRoleAndStatus(role, UserStatus.ACTIVE);
        return ResponseEntity.ok(response);
    }

    // Actualizacion del perfil de un usuario
    @PutMapping("/me/complete")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> updateOwnProfileComplete(
            @Valid @RequestBody UserSelfUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("User {} updating complete profile", userDetails.getUsername());

        // Usar updateSelf en UserService
        UserResponse response = userService.updateSelf(userDetails.getId(), request);
        return ResponseEntity.ok(response);
    }
}