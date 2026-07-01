package com.lanely.api.dto.me;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "ChangePasswordRequest", description = "Change the current account's password")
public record ChangePasswordRequest(

        @Schema(description = "Current password, for verification", example = "S3cretPass", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String currentPassword,

        @Schema(description = "New password, at least 8 characters", example = "N3wS3cretPass", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 8)
        @NotBlank
        @Size(min = 8, max = 100)
        String newPassword
) {
}
