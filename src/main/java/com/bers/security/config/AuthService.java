package com.bers.security.config;

import com.bers.security.config.AuthDtos.*;

public interface AuthService {
    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(RefreshTokenRequest request);

    MessageResponse changePassword(String userEmail, ChangePasswordRequest request);

    MessageResponse forgotPassword(ForgotPasswordRequest request);

    MessageResponse resetPassword(ResetPasswordRequest request);

    MessageResponse logout(LogoutRequest request);

    TokenValidationResponse validateToken(String token);
}
