package com.bers.security.config;

import com.bers.security.error.Http401EntryPoint;
import com.bers.security.error.Http403AccessDenied;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(jsr250Enabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    // Endpoints publicos sin patrones con {id}
    private static final String[] PUBLIC_ENDPOINTS = {
            "/api/auth/**",
            "/api/v1/routes/origins",
            "/api/v1/routes/destinations",
            "/api/v1/trips/filter",
            "/api/v1/seats/*/full-seats-and-holds",
            "/api/v1/trips/search",
            "/api/v1/trips/search/*",
            "/api/v1/trips/*/details",
            "/api/v1/trips/route/*",
            "/api/v1/trips/today",
            "/api/v1/trips/today/active",
            "/api/v1/routes/all",
            "/api/v1/routes/*",
            "/api/v1/routes/origin/*/destination/*",
            "/api/v1/routes/*/stops",
            "/api/v1/seats/*/full-seats-and-holds",
            "/api/v1/seats/bus/*",
            "/api/v1/seats/bus/*/number/*",
            "/api/v1/seats/bus/*/count",
            "/api/v1/stops/search",
            "/api/v1/stops/all",
            "/api/v1/stops/{id}",
            "/api/v1/stops/route/*",
            "/api/v1/fare-rules/calculate",
            "/api/v1/seats/trips/*/seats",
            "/api/v1/parcels/create",

            "/actuator/health",
            "/actuator/info",
            "/api-docs/**",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-ui.html"
    };
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService customUserDetailsService;
    private final Http401EntryPoint http401EntryPoint;
    private final Http403AccessDenied http403AccessDenied;
    @Value("${cors.allowed-origins:http://localhost:3000,http://localhost:4200}")
    private String allowedOrigins;
    @Value("${app.security.enable-csrf:false}")
    private boolean enableCsrf;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                //  CSRF deshabilitado para APIs REST
                .csrf(AbstractHttpConfigurer::disable)

                //  CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                //  MANEJO DE EXCEPCIONES
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(http401EntryPoint)
                        .accessDeniedHandler(http403AccessDenied)
                )

                //  POLÍTICA DE SESIONES
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                //  AUTORIZACIÓN JERÁRQUICA CORREGIDA
                .authorizeHttpRequests(auth -> auth
                        // Públicos
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()

                        //  Endpoint específico para stops de ruta
                        .requestMatchers("/api/v1/routes/*/stops").permitAll()

                        // Actuator solo para ADMIN
                        .requestMatchers("/actuator/**").hasRole("ADMIN")

                        //  Clerk endpoints - agrupados por tipo
                        .requestMatchers("/api/v1/tickets/**").hasAnyRole("PASSENGER", "CLERK", "DRIVER", "DISPATCHER", "ADMIN")
                        .requestMatchers("/api/v1/baggage/**").hasAnyRole("CLERK", "ADMIN")
                        .requestMatchers("/api/v1/parcels/**").hasAnyRole("CLERK", "ADMIN", "PASSENGER", "DRIVER")
                        .requestMatchers("/api/v1/seat-holds/**").hasAnyRole("CLERK", "ADMIN", "PASSENGER")

                        //  Driver endpoints - patrones específicos
                        .requestMatchers("/api/v1/trips/*/depart").hasAnyRole("DRIVER", "ADMIN", "DISPATCHER")
                        .requestMatchers("/api/v1/trips/*/boarding/**").hasAnyRole("DRIVER", "ADMIN", "DISPATCHER")
                        .requestMatchers("/api/v1/parcels/*/delivered").hasAnyRole("DRIVER", "ADMIN", "DISPATCHER")
                        .requestMatchers("/api/v1/tickets/*/used").hasAnyRole("DRIVER", "ADMIN", "DISPATCHER")
                        .requestMatchers("/api/v1/tickets/*/no-show").hasAnyRole("DRIVER", "ADMIN", "DISPATCHER")
                        .requestMatchers("/api/v1/trips/driver/**").hasAnyRole("DRIVER", "ADMIN", "DISPATCHER")

                        //  Dispatcher endpoints
                        .requestMatchers("/api/v1/assignments/**").hasAnyRole("DISPATCHER", "ADMIN")
                        .requestMatchers("/api/v1/trips/*/assign").hasAnyRole("DISPATCHER", "ADMIN")
                        .requestMatchers("/api/v1/buses/**").hasAnyRole("DISPATCHER", "ADMIN")
                        .requestMatchers("/api/v1/routes/**").hasAnyRole("DISPATCHER", "ADMIN")
                        .requestMatchers("/api/v1/overbooking/**").hasAnyRole("DISPATCHER", "ADMIN", "CLERK")

                        // Resto de endpoints requieren autenticación
                        .requestMatchers("/api/v1/**").authenticated()
                        .requestMatchers("/test/**").permitAll()

                        .anyRequest().authenticated()
                )

                .userDetailsService(customUserDetailsService)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Usar Arrays.asList  para mutabilidad
        List<String> origins = Arrays.asList(allowedOrigins.split(","));

        config.setAllowedOrigins(origins.stream()
                .map(String::trim)
                .filter(origin -> !origin.isBlank())
                .toList());

        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        //  Usar Arrays.asList para headers
        config.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "Accept",
                "X-Requested-With",
                "X-Offline-Mode",
                "X-Sync-Token"
        ));

        config.setAllowCredentials(true);

        //  Usar Arrays.asList para headers expuestos
        config.setExposedHeaders(Arrays.asList(
                "Authorization",
                "X-Refresh-Token",
                "X-Token-Expires-In"
        ));

        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}