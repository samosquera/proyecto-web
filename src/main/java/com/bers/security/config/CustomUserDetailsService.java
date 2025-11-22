package com.bers.security.config;

import com.bers.domain.entities.User;
import com.bers.domain.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Loading user by email: {}", email);

        User user = userRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> {
                    log.warn("User not found with email: {}", email);
                    return new UsernameNotFoundException("User not found with email: " + email);
                });

        log.debug("User found: {} with role: {}", user.getEmail(), user.getRole());
        return new CustomUserDetails(user);
    }

    @Transactional(readOnly = true)
    public UserDetails loadUserByPhone(String phone) throws UsernameNotFoundException {
        log.debug("Loading user by phone: {}", maskPhone(phone));

        User user = userRepository.findByPhone(phone.trim())
                .orElseThrow(() -> {
                    log.warn("User not found with phone: {}", maskPhone(phone));
                    return new UsernameNotFoundException("User not found with phone: " + maskPhone(phone));
                });

        log.debug("User found with phone: {} - email: {}", maskPhone(phone), user.getEmail());
        return new CustomUserDetails(user);
    }

    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long userId) throws UsernameNotFoundException {
        log.debug("Loading user by ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found with ID: {}", userId);
                    return new UsernameNotFoundException("User not found with ID: " + userId);
                });

        return new CustomUserDetails(user);
    }

    // Metodo para verificar permisos específicos
    public boolean hasPermission(Long userId, String permission) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) loadUserById(userId);
            return userDetails.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals(permission));
        } catch (UsernameNotFoundException e) {
            return false;
        }
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 6) {
            return "***";
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 3);
    }
}