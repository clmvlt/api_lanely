package com.lanely.api.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "ResetPasswordRequest", description = "Payload to set a new password from the token contained in the reset link")
public record ResetPasswordRequest(

        @Schema(description = "Reset token from the email link (?token=...)", example = "Zm9vYmFyYmF6cXV4MTIzNDU2Nzg", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String token,

        @Schema(description = "New plain-text password, at least 8 characters", example = "N3wS3cretPass", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 8)
        @NotBlank
        @Size(min = 8, max = 100)
        String newPassword
) {
}
