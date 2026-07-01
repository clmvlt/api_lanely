package com.lanely.api.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "ForgotPasswordRequest", description = "Payload to request a password reset link by email")
public record ForgotPasswordRequest(

        @Schema(description = "Email address of the account to reset (case-insensitive)", example = "jane.doe@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        @Email
        String email
) {
}
