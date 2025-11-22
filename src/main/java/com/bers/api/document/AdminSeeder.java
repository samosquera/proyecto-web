package com.bers.api.document;

import com.bers.domain.entities.User;
import com.bers.domain.entities.enums.UserRole;
import com.bers.domain.entities.enums.UserStatus;
import com.bers.domain.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
public record AdminSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) implements CommandLineRunner {

    @Override
    public void run(String... args) {
        if (userRepository.findByEmail("sagbar@gmail.com").isEmpty()) {
            User admin = User.builder()
                    .username("sag bar")
                    .email("sagbar@gmail.com")
                    .phone("3104386829")
                    .dateOfBirth(LocalDate.of(2001, 03, 11))
                    .passwordHash(passwordEncoder.encode("admin123"))
                    .role(UserRole.ADMIN)
                    .status(UserStatus.ACTIVE)
                    .build();
            userRepository.save(admin);
            log.info("Admin user created: {} / ADMIN", admin.getEmail());
        }
    }
}
