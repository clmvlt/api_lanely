package com.lanely.api.dto.profile;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(name = "ProfileResponse", description = "A mobile delivery profile belonging to a company")
public record ProfileResponse(

        @Schema(description = "Unique profile identifier", example = "8a1f2c3b-4d5e-6f70-3f1c-8d2e9b4a4d6e")
        UUID id,

        @Schema(description = "Profile username (unique within its company)", example = "driver01")
        String username,

        @Schema(description = "Optional display name", example = "John (morning shift)", nullable = true)
        String displayName,

        @Schema(description = "Whether the profile is allowed to log in", example = "true")
        boolean active
) {
}
