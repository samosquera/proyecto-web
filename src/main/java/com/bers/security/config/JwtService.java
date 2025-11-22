package com.bers.security.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    @Value("${jwt.issuer:bers-app}")
    private String issuer;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Long extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("userId", Long.class);
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    public String extractTokenType(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("type", String.class);
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        final Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.debug("Token expired: {}", e.getMessage());
            throw new JwtException("Token expired");
        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported JWT token: {}", e.getMessage());
            throw new JwtException("Unsupported token");
        } catch (MalformedJwtException e) {
            log.warn("Malformed JWT token: {}", e.getMessage());
            throw new JwtException("Malformed token");
        } catch (SignatureException e) {
            log.warn("Invalid JWT signature: {}", e.getMessage());
            throw new JwtException("Invalid signature");
        } catch (IllegalArgumentException e) {
            log.warn("JWT claims string is empty: {}", e.getMessage());
            throw new JwtException("Empty claims");
        }
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        if (userDetails instanceof CustomUserDetails customUser) {
            claims.put("userId", customUser.getId());
            claims.put("role", customUser.getRole());
            claims.put("status", customUser.getStatus().name());
            claims.put("username", customUser.getUsername());
            claims.put("phone", customUser.getPhone());
            claims.put("type", "access");

            claims.put("authorities", customUser.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList()));
        }

        log.debug("Generating access token for user: {}", userDetails.getUsername());
        return buildToken(claims, userDetails, jwtExpiration);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        if (userDetails instanceof CustomUserDetails customUser) {
            claims.put("userId", customUser.getId());
            claims.put("type", "refresh");
        }

        log.debug("Generating refresh token for user: {}", userDetails.getUsername());
        return buildToken(claims, userDetails, refreshExpiration);
    }

    public String generateOfflineToken(UserDetails userDetails, Long durationMs) {
        Map<String, Object> claims = new HashMap<>();

        if (userDetails instanceof CustomUserDetails customUser) {
            claims.put("userId", customUser.getId());
            claims.put("role", customUser.getRole());
            claims.put("type", "offline");
            claims.put("offline", true);
        }

        log.debug("Generating offline token for user: {}", userDetails.getUsername());
        return buildToken(claims, userDetails, durationMs);
    }

    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
        long currentTimeMillis = System.currentTimeMillis();

        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuer(issuer)
                .setIssuedAt(new Date(currentTimeMillis))
                .setExpiration(new Date(currentTimeMillis + expiration))
                .setId(java.util.UUID.randomUUID().toString())
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            final String tokenType = extractTokenType(token);

            if (!"access".equals(tokenType) && !"offline".equals(tokenType)) {
                log.warn("Invalid token type: {}", tokenType);
                return false;
            }

            boolean usernameMatches = username.equals(userDetails.getUsername());
            boolean notExpired = !isTokenExpired(token);
            boolean accountActive = userDetails.isEnabled();

            return usernameMatches && notExpired && accountActive;

        } catch (JwtException e) {
            log.debug("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public boolean isRefreshTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            String tokenType = extractTokenType(token);

            if (!"refresh".equals(tokenType)) {
                log.warn("Token is not a refresh token");
                return false;
            }

            boolean usernameMatches = username.equals(userDetails.getUsername());
            boolean notExpired = !isTokenExpired(token);
            boolean accountActive = userDetails.isEnabled();

            return usernameMatches && notExpired && accountActive;

        } catch (JwtException e) {
            log.debug("Refresh token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public boolean isOfflineTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            String tokenType = extractTokenType(token);

            if (!"offline".equals(tokenType)) {
                log.warn("Token is not an offline token");
                return false;
            }

            boolean usernameMatches = username.equals(userDetails.getUsername());
            boolean notExpired = !isTokenExpired(token);
            boolean accountActive = userDetails.isEnabled();

            return usernameMatches && notExpired && accountActive;

        } catch (JwtException e) {
            log.debug("Offline token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (JwtException e) {
            log.debug("Error checking token expiration: {}", e.getMessage());
            return true;
        }
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public Date extractIssuedAt(String token) {
        return extractClaim(token, Claims::getIssuedAt);
    }

    public Long getExpirationTime() {
        return jwtExpiration / 1000;
    }

    public Long getRefreshExpirationTime() {
        return refreshExpiration / 1000;
    }

    public Long getTimeUntilExpiration(String token) {
        try {
            Date expiration = extractExpiration(token);
            long now = System.currentTimeMillis();
            long expirationMillis = expiration.getTime();
            long remaining = expirationMillis - now;

            return remaining > 0 ? remaining / 1000 : 0L;

        } catch (JwtException e) {
            log.debug("Error calculating time until expiration: {}", e.getMessage());
            return 0L;
        }
    }

    public Map<String, Object> getTokenInfo(String token) {
        Map<String, Object> info = new HashMap<>();

        try {
            Claims claims = extractAllClaims(token);

            info.put("subject", claims.getSubject());
            info.put("userId", claims.get("userId"));
            info.put("role", claims.get("role"));
            info.put("type", claims.get("type"));
            info.put("username", claims.get("username"));
            info.put("authorities", claims.get("authorities"));
            info.put("issuedAt", claims.getIssuedAt());
            info.put("expiresAt", claims.getExpiration());
            info.put("isExpired", isTokenExpired(token));
            info.put("secondsRemaining", getTimeUntilExpiration(token));
            info.put("offline", claims.get("offline", Boolean.class));

        } catch (JwtException e) {
            info.put("error", e.getMessage());
            info.put("valid", false);
        }

        return info;
    }

    public boolean hasValidFormat(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        String[] parts = token.split("\\.");
        return parts.length == 3;
    }

    public String extractTokenFromHeader(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            return null;
        }

        if (authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }

        return null;
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public boolean isTokenExpiringSoon(String token) {
        Long secondsRemaining = getTimeUntilExpiration(token);
        return secondsRemaining > 0 && secondsRemaining < 300;
    }

    public String getTokenSummary(String token) {
        try {
            String username = extractUsername(token);
            String role = extractRole(token);
            String type = extractTokenType(token);
            Long secondsRemaining = getTimeUntilExpiration(token);

            return String.format("[Type: %s, User: %s, Role: %s, Expires in: %ds]",
                    type != null ? type : "access",
                    username,
                    role,
                    secondsRemaining
            );

        } catch (JwtException e) {
            return "[Invalid Token]";
        }
    }
}