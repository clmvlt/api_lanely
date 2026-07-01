package com.lanely.api.dto.me;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(name = "ProfileMe", description = "Identity of the current mobile delivery profile")
public record ProfileMe(

        @Schema(description = "Profile identifier", example = "8a1f2c3b-4d5e-6f70-3f1c-8d2e9b4a4d6e")
        UUID id,

        @Schema(description = "Profile username", example = "driver01")
        String username,

        @Schema(description = "Optional display name", example = "John (morning shift)", nullable = true)
        String displayName,

        @Schema(description = "Whether the profile is active", example = "true")
        boolean active,

        @Schema(description = "Identifier of the company the profile belongs to", example = "11112222-3333-4444-5555-666677778888")
        UUID companyId,

        @Schema(description = "Name of the company the profile belongs to", example = "Speedy Delivery")
        String companyName
) {
}
