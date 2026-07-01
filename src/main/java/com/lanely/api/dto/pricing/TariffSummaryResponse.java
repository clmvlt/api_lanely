package com.lanely.api.dto.pricing;

import com.lanely.api.entity.enums.TariffStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Schema(name = "TariffSummaryResponse", description = "A rate card as shown in a list")
public record TariffSummaryResponse(

        @Schema(description = "Unique identifier of the rate card", example = "9f1c2d3e-4a5b-6c7d-8e9f-0a1b2c3d4e5f")
        UUID id,

        @Schema(description = "Display name", example = "Standard national 2026")
        String name,

        @Schema(description = "ISO 4217 currency code", example = "EUR")
        String currency,

        @Schema(description = "Whether this is the company default grid", example = "true")
        boolean isDefault,

        @Schema(description = "Lifecycle status", example = "ACTIVE")
        TariffStatus status,

        @Schema(description = "Client this grid is specific to, or null for a company-wide grid", example = "7a1c2d3e-4a5b-6c7d-8e9f-0a1b2c3d4e5f", nullable = true)
        UUID clientId,

        @Schema(type = "string", format = "date", description = "Start of the validity window (inclusive)", example = "2026-01-01", nullable = true)
        LocalDate validFrom,

        @Schema(type = "string", format = "date", description = "End of the validity window (inclusive)", example = "2026-12-31", nullable = true)
        LocalDate validUntil,

        @Schema(type = "string", format = "date-time", description = "Creation instant (ISO-8601 UTC)", example = "2026-06-10T09:15:30Z")
        Instant createdAt
) {
}
