package com.lanely.api.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "TokenResponse", description = "Access and refresh tokens issued for an authenticated session")
public record TokenResponse(

        @Schema(description = "Short-lived JWT access token to send in the Authorization header as 'Bearer <token>'", example = "eyJhbGciOiJIUzI1NiJ9...")
        String accessToken,

        @Schema(description = "Token type to use in the Authorization header", example = "Bearer")
        String tokenType,

        @Schema(description = "Access token lifetime in seconds", example = "900")
        long accessExpiresInSeconds,

        @Schema(description = "Opaque refresh token used to obtain a new pair via /auth/refresh", example = "y2c8...opaque-token")
        String refreshToken,

        @Schema(description = "Refresh token lifetime in seconds", example = "2592000")
        long refreshExpiresInSeconds
) {
}
