package com.bers.security.config;

import com.bers.domain.entities.User;
import com.bers.domain.entities.enums.UserRole;
import com.bers.domain.entities.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

@Getter
@AllArgsConstructor
@Builder
public class CustomUserDetails implements UserDetails {

    private final Long id;
    private final String userEmail;
    private final String password;
    private final UserRole role;
    private final UserStatus status;
    private final Collection<? extends GrantedAuthority> authorities;

    private final String displayName;
    private final String phone;

    public CustomUserDetails(User user) {
        this.id = user.getId();
        this.userEmail = user.getEmail();
        this.password = user.getPasswordHash();
        this.role = user.getRole();
        this.status = user.getStatus();
        this.displayName = user.getUsername();
        this.phone = user.getPhone();

        // Authorities más granulares
        this.authorities = buildAuthorities(user);
    }

    private Collection<? extends GrantedAuthority> buildAuthorities(User user) {
        Set<GrantedAuthority> authorities = new java.util.HashSet<>();

        // Rol básico
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

        // Permisos específicos basados en el rol
        switch (user.getRole()) {
            case ADMIN:
                authorities.add(new SimpleGrantedAuthority("PERMISSION_READ_ALL"));
                authorities.add(new SimpleGrantedAuthority("PERMISSION_WRITE_ALL"));
                authorities.add(new SimpleGrantedAuthority("PERMISSION_MANAGE_USERS"));
                break;
            case DISPATCHER:
                authorities.add(new SimpleGrantedAuthority("PERMISSION_MANAGE_TRIPS"));
                authorities.add(new SimpleGrantedAuthority("PERMISSION_MANAGE_ASSIGNMENTS"));
                authorities.add(new SimpleGrantedAuthority("PERMISSION_APPROVE_OVERBOOKING"));
                break;
            case CLERK:
                authorities.add(new SimpleGrantedAuthority("PERMISSION_SELL_TICKETS"));
                authorities.add(new SimpleGrantedAuthority("PERMISSION_MANAGE_PARCELS"));
                authorities.add(new SimpleGrantedAuthority("PERMISSION_OFFLINE_OPERATION"));
                break;
            case DRIVER:
                authorities.add(new SimpleGrantedAuthority("PERMISSION_VALIDATE_TICKETS"));
                authorities.add(new SimpleGrantedAuthority("PERMISSION_UPDATE_TRIP_STATUS"));
                authorities.add(new SimpleGrantedAuthority("PERMISSION_OFFLINE_OPERATION"));
                break;
            case PASSENGER:
                authorities.add(new SimpleGrantedAuthority("PERMISSION_BUY_TICKETS"));
                authorities.add(new SimpleGrantedAuthority("PERMISSION_VIEW_TRIPS"));
                break;
        }

        return Collections.unmodifiableSet(authorities);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return userEmail;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return status != UserStatus.BLOCKED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status == UserStatus.ACTIVE;
    }

    public boolean hasRole(String role) {
        return this.role.name().equalsIgnoreCase(role);
    }

    public boolean hasAnyRole(String... roles) {
        return java.util.Arrays.stream(roles)
                .anyMatch(r -> this.role.name().equalsIgnoreCase(r));
    }

    public boolean hasPermission(String permission) {
        return authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals(permission));
    }

    public String getDisplayUsername() {
        return displayName;
    }
}