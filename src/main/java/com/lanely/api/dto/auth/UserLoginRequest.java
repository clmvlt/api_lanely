package com.lanely.api.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "UserLoginRequest", description = "Credentials to authenticate a web user")
public record UserLoginRequest(

        @Schema(description = "Email address used at registration (case-insensitive)", example = "jane.doe@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        @Email
        String email,

        @Schema(description = "Account password", example = "S3cretPass", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String password
) {
}
