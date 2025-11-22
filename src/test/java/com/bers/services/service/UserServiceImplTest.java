package com.bers.services.service;

import com.bers.api.dtos.UserDtos.*;
import com.bers.domain.entities.User;
import com.bers.domain.entities.enums.UserRole;
import com.bers.domain.entities.enums.UserStatus;
import com.bers.domain.repositories.UserRepository;
import com.bers.services.mappers.UserMapper;
import com.bers.services.service.serviceImple.UserServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService test")
class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @Spy
    private UserMapper userMapper = Mappers.getMapper(UserMapper.class);
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private UserServiceImpl userService;
    private User user;
    private UserCreateRequest createRequest;
    private UserUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("John Doe")
                .email("john@example.com")
                .phone("3001234567")
                .role(UserRole.PASSENGER)
                .status(UserStatus.ACTIVE)
                .passwordHash("hashedPassword")
                .createAt(LocalDateTime.now())
                .build();

        createRequest = new UserCreateRequest(
                "John Doe",
                "john@example.com",
                "3001234567",
                UserRole.PASSENGER,
                "password123",
                LocalDate.of(2000,03,27)
        );

        updateRequest = new UserUpdateRequest(
                "Jane Doe",
                "3009876543",
                UserStatus.ACTIVE
        );
    }

    @Test
    @DisplayName("Debería crear un usuario ")
    void shouldCreateUser() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByPhone(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponse result = userService.create(createRequest);

        assertNotNull(result);
        Assertions.assertEquals(user.getId(), result.id());
        Assertions.assertEquals(user.getEmail(), result.email());
        verify(userRepository).existsByEmail(createRequest.email());
        verify(userRepository).existsByPhone(createRequest.phone());
        verify(passwordEncoder).encode(createRequest.password());
        verify(userRepository).save(any(User.class));
        verify(userMapper).toEntity(createRequest);
        verify(userMapper).toResponse(any(User.class));
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando el email ya existe")
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.create(createRequest)
        );

        Assertions.assertTrue(exception.getMessage().contains("Email already exists"));
        verify(userRepository).existsByEmail(createRequest.email());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando el teléfono ya existe")
    void shouldThrowExceptionWhenPhoneAlreadyExists() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByPhone(anyString())).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.create(createRequest)
        );

        Assertions.assertTrue(exception.getMessage().contains("Phone already exists"));
        verify(userRepository).existsByPhone(createRequest.phone());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Debería actualizar un usuario ")
    void shouldUpdateUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByPhone(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponse result = userService.update(1L, updateRequest);

        assertNotNull(result);
        verify(userRepository).findById(1L);
        verify(userMapper).updateEntity(updateRequest, user);
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Debería lanzar excepción al actualizar usuario inexistente")
    void shouldThrowExceptionWhenUpdatingNonexistentUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.update(1L, updateRequest)
        );

        Assertions.assertTrue(exception.getMessage().contains("User not found"));
        verify(userRepository).findById(1L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Debería obtener usuario por ID")
    void shouldGetUserById() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse result = userService.getById(1L);

        assertNotNull(result);
        Assertions.assertEquals(user.getId(), result.id());
        verify(userRepository).findById(1L);
        verify(userMapper).toResponse(user);
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando usuario no existe por ID")
    void shouldThrowExceptionWhenUserDoesNotExistById() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

         IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.getById(1L)
        );

        Assertions.assertTrue(exception.getMessage().contains("User not found"));
        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("Debería obtener usuario por email")
    void shouldGetUserByEmail() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        UserResponse result = userService.getByEmail("john@example.com");

        assertNotNull(result);
        Assertions.assertEquals(user.getEmail(), result.email());
        verify(userRepository).findByEmail("john@example.com");
    }

    @Test
    @DisplayName("Debería obtener usuario por teléfono")
    void shouldobtainUserByPhone() {
        when(userRepository.findByPhone(anyString())).thenReturn(Optional.of(user));

       UserResponse result = userService.getByPhone("3001234567");

        assertNotNull(result);
        Assertions.assertEquals(user.getPhone(), result.phone());
        verify(userRepository).findByPhone("3001234567");
    }

    @Test
    @DisplayName("Debería obtener todos los usuarios")
    void shouldGetAllUsers() {
        List<User> users = Arrays.asList(user, user);
        when(userRepository.findAll()).thenReturn(users);

        List<UserResponse> result = userService.getAll();

        assertNotNull(result);
        Assertions.assertEquals(2, result.size());
        verify(userRepository).findAll();
        verify(userMapper, times(2)).toResponse(any(User.class));
    }

    @Test
    @DisplayName("Debería obtener usuarios por rol")
    void shouldGetUsersByRole() {
        List<User> users = Arrays.asList(user);
        when(userRepository.findByRole(UserRole.PASSENGER)).thenReturn(users);

        List<UserResponse> result = userService.getByRole(UserRole.PASSENGER);

        assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        verify(userRepository).findByRole(UserRole.PASSENGER);
    }

    @Test
    @DisplayName("Debería obtener usuarios por rol y estado")
    void shouldGetUsersByRoleAndStatus() {
        List<User> users = Arrays.asList(user);
        when(userRepository.findByRoleAndStatus(UserRole.PASSENGER, UserStatus.ACTIVE))
                .thenReturn(users);

        List<UserResponse> result = userService.getByRoleAndStatus(
                UserRole.PASSENGER, UserStatus.ACTIVE
        );

        assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        verify(userRepository).findByRoleAndStatus(UserRole.PASSENGER, UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("Debería eliminar un usuario")
    void shouldDeleteAUser() {
        when(userRepository.existsById(1L)).thenReturn(true);

         userService.delete(1L);

        verify(userRepository).existsById(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Debería lanzar excepción al eliminar usuario inexistente")
    void shouldThrowExceptionWhenDeletingNonexistentUser() {
        when(userRepository.existsById(1L)).thenReturn(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.delete(1L)
        );

        Assertions.assertTrue(exception.getMessage().contains("User not found"));
        verify(userRepository).existsById(1L);
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Debería verificar si existe email")
    void shouldCheckIfEmailExists() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        boolean result = userService.existsByEmail("john@example.com");

        Assertions.assertTrue(result);
        verify(userRepository).existsByEmail("john@example.com");
    }

    @Test
    @DisplayName("Debería verificar si existe teléfono")
    void shouldCheckIfPhoneExists() {
        when(userRepository.existsByPhone(anyString())).thenReturn(true);

        boolean result = userService.existsByPhone("3001234567");

        Assertions.assertTrue(result);
        verify(userRepository).existsByPhone("3001234567");
    }

    @Test
    @DisplayName("Debería cambiar el estado del usuario")
    void shouldchangeUserStatus() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponse result = userService.changeStatus(1L, UserStatus.INACTIVE);
        assertNotNull(result);
        verify(userRepository).findById(1L);
        verify(userRepository).save(user);
        Assertions.assertEquals(UserStatus.INACTIVE, user.getStatus());
    }
}