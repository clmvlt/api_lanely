package com.lanely.api.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "RefreshRequest", description = "Payload to exchange a valid refresh token for a new access/refresh token pair")
public record RefreshRequest(

        @Schema(description = "Opaque refresh token returned at login or by a previous refresh", example = "y2c8...rotated-opaque-token", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String refreshToken
) {
}
