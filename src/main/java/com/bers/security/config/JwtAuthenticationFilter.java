package com.bers.security.config;

import com.bers.security.config.service.TokenBlacklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        //  Modo offline
        String offlineMode = request.getHeader("X-Offline-Mode");
        if ("true".equals(offlineMode)) {
            log.debug("Modo offline detectado para: {}", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);

        // validar si el token está en blacklist antes de seguir
        if (tokenBlacklistService.isBlacklisted(jwt)) {
            log.warn("Intento de usar token en blacklist");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token is blacklisted");
            return;
        }

        try {
            username = jwtService.extractUsername(jwt);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                //  Validación completa del token
                if (jwtService.isTokenValid(jwt, userDetails)) {

                    // Validar estado de la cuenta
                    if (userDetails instanceof CustomUserDetails customUser) {
                        if (!customUser.isEnabled()) {
                            log.warn("Account is not active for user: {}", username);
                            response.sendError(HttpStatus.FORBIDDEN.value(), "Account is not active");
                            return;
                        }
                    }

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    log.debug("Authenticated user: {} for URI: {}", username, request.getRequestURI());
                } else {
                    log.warn("Invalid JWT token for user: {}", username);
                }
            }
        } catch (Exception e) {
            log.error("JWT authentication failed: {}", e.getMessage());
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                response.sendError(HttpStatus.UNAUTHORIZED.value(), "Invalid authentication token");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    // Endpoints públicos del filtro JWT
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/auth/") ||
                path.startsWith("/actuator/health") ||
                path.startsWith("/api-docs") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/test/");
    }
}
