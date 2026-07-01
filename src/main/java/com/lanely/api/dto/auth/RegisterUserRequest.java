package com.lanely.api.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "RegisterUserRequest", description = "Payload to create a new web user account")
public record RegisterUserRequest(

        @Schema(description = "Unique email address of the user (case-insensitive)", example = "jane.doe@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        @Email
        String email,

        @Schema(description = "Plain-text password, at least 8 characters", example = "S3cretPass", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 8)
        @NotBlank
        @Size(min = 8, max = 100)
        String password,

        @Schema(description = "First name of the user", example = "Jane", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        @Size(max = 100)
        String firstName,

        @Schema(description = "Last name of the user", example = "Doe", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        @Size(max = 100)
        String lastName
) {
}
