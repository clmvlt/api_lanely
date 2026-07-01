package com.lanely.api.dto.profile;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "CreateProfileRequest", description = "Payload to create a mobile delivery profile within a company")
public record CreateProfileRequest(

        @Schema(description = "Username for the profile, unique within the company (the same username may exist in other companies)", example = "driver01", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        @Size(max = 100)
        String username,

        @Schema(description = "Plain-text password for the profile, at least 6 characters", example = "drivpass", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 6)
        @NotBlank
        @Size(min = 6, max = 100)
        String password,

        @Schema(description = "Optional human-friendly display name", example = "John (morning shift)", nullable = true)
        @Size(max = 150)
        String displayName
) {
}
