package com.lanely.api.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "VerifyEmailRequest", description = "Payload to confirm an email address from the token contained in the verification link")
public record VerifyEmailRequest(

        @Schema(description = "Verification token from the email link (?token=...)", example = "Zm9vYmFyYmF6cXV4MTIzNDU2Nzg", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String token
) {
}
