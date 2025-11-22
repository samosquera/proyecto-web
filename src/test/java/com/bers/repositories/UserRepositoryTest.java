package com.bers.repositories;

import com.bers.AbstractRepositoryTest;
import com.bers.domain.entities.User;
import com.bers.domain.entities.enums.UserRole;
import com.bers.domain.entities.enums.UserStatus;
import com.bers.domain.repositories.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class UserRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Guardar un User")
    void shouldSaveUser() {
        var user = User.builder()
                .username("Juan Pérez")
                .email("juan@example.com")
                .phone("3001234567")
                .role(UserRole.PASSENGER)
                .status(UserStatus.ACTIVE)
                .passwordHash("hashedPassword123")
                .build();

        User saved = userRepository.save(user);
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUsername()).isEqualTo("Juan Pérez");
        assertThat(saved.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(saved.getCreateAt()).isNotNull();
    }

    @Test
    @DisplayName("Encontrar user por email")
    void shouldFindUserByEmail() {
        var user =  User.builder()
                .username("Maria Garcia")
                .email("maria@example.com")
                .phone("3009876543")
                .role(UserRole.PASSENGER)
                .status(UserStatus.ACTIVE)
                .passwordHash("hash")
                .build();
        userRepository.save(user);

        Optional<User> found = userRepository.findByEmail("maria@example.com");

        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("Maria Garcia");
    }

    @Test
    @DisplayName("Buscar email no existente")
    void shouldNotFindUserByNonExistentEmail() {
        Optional<User> found = userRepository.findByEmail("noexiste@example.com");
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Buscarr user por phone")
    void shouldFindUserByPhone() {
        var user = User.builder()
                .username("Pedro Lopez")
                .email("pedro@example.com")
                .phone("3112223344")
                .role(UserRole.DRIVER)
                .status(UserStatus.ACTIVE)
                .passwordHash("hash")
                .build();
        userRepository.save(user);

        Optional<User> found = userRepository.findByPhone("3112223344");

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("pedro@example.com");
    }

    @Test
    @DisplayName("Verificar si email existe")
    void shouldCheckIfEmailExists() {
        var user = User.builder()
                .username("Test User")
                .email("test@example.com")
                .phone("3001111111")
                .role(UserRole.PASSENGER)
                .status(UserStatus.ACTIVE)
                .passwordHash("hash")
                .build();
        userRepository.save(user);

        boolean exists = userRepository.existsByEmail("test@example.com");
        boolean notExists = userRepository.existsByEmail("otro@example.com");

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Verificar si existe phone")
    void shouldCheckIfPhoneExists() {
        var user = User.builder()
                .username("Phone User")
                .email("phone@example.com")
                .phone("3002222222")
                .role(UserRole.CLERK)
                .status(UserStatus.ACTIVE)
                .passwordHash("hash")
                .build();
        userRepository.save(user);
        boolean exists = userRepository.existsByPhone("3002222222");
        boolean notExists = userRepository.existsByPhone("3009999999");

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Listar user por role")
    void shouldFindUsersByRole() {

        var passenger1 = User.builder()
                .username("Passenger 1")
                .email("p1@example.com")
                .phone("3001111111")
                .role(UserRole.PASSENGER)
                .status(UserStatus.ACTIVE)
                .passwordHash("hash")
                .build();

        var passenger2 = User.builder()
                .username("Passenger 2")
                .email("p2@example.com")
                .phone("3002222222")
                .role(UserRole.PASSENGER)
                .status(UserStatus.ACTIVE)
                .passwordHash("hash")
                .build();

        var driver = User.builder()
                .username("Driver")
                .email("driver@example.com")
                .phone("3003333333")
                .role(UserRole.DRIVER)
                .status(UserStatus.ACTIVE)
                .passwordHash("hash")
                .build();

        userRepository.saveAll(List.of(passenger1, passenger2, driver));

        List<User> passengers = userRepository.findByRole(UserRole.PASSENGER);
        List<User> drivers = userRepository.findByRole(UserRole.DRIVER);

        assertThat(passengers).hasSize(2);
        assertThat(drivers).hasSize(1);
        assertThat(drivers.getFirst().getEmail()).isEqualTo("driver@example.com");
    }

    @Test
    @DisplayName("Listar user por role y estado")
    void shouldFindUsersByRoleAndStatus() {

        var activePassenger = User.builder()
                .username("Active Passenger")
                .email("active@example.com")
                .phone("3001111111")
                .role(UserRole.PASSENGER)
                .status(UserStatus.ACTIVE)
                .passwordHash("hash")
                .build();

        var inactivePassenger = User.builder()
                .username("Inactive Passenge")
                .email("inactive@example.com")
                .phone("3002222222")
                .role(UserRole.PASSENGER)
                .status(UserStatus.INACTIVE)
                .passwordHash("hash")
                .build();

        userRepository.saveAll(List.of(activePassenger, inactivePassenger));

        List<User> activeUsers = userRepository.findByRoleAndStatus(
                UserRole.PASSENGER,UserStatus.ACTIVE);
        List<User> inactiveUsers = userRepository.findByRoleAndStatus(
                UserRole.PASSENGER, UserStatus.INACTIVE);

        assertThat(activeUsers).hasSize(1);
        assertThat(inactiveUsers).hasSize(1);
        assertThat(activeUsers.getFirst().getEmail()).isEqualTo("active@example.com");
    }
}