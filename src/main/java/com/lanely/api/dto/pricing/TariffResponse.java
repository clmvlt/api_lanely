package com.lanely.api.dto.pricing;

import com.lanely.api.entity.enums.TariffStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Schema(name = "TariffResponse", description = "A full rate card (tariff) with its components and fuel surcharge policy. All monetary amounts are tax-excluded (HT).")
public record TariffResponse(

        @Schema(description = "Unique identifier of the rate card", example = "9f1c2d3e-4a5b-6c7d-8e9f-0a1b2c3d4e5f")
        UUID id,

        @Schema(description = "Display name", example = "Standard national 2026")
        String name,

        @Schema(description = "Free-form description", example = "Default grid applied to national deliveries", nullable = true)
        String description,

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

        @Schema(description = "Rounding mode applied to the final total", example = "HALF_UP")
        RoundingMode roundingMode,

        @Schema(description = "Number of decimals kept on the final total", example = "2")
        int roundingScale,

        @Schema(description = "Minimum charge applied as a floor to the total, tax-excluded (HT)", example = "15.00", nullable = true)
        BigDecimal minChargeAmount,

        @Schema(description = "Billable lines, ordered by position")
        List<TariffComponentDto> components,

        @Schema(description = "Fuel indexation policy, or null if none configured", nullable = true)
        FuelSurchargePolicyResponse fuelSurcharge,

        @Schema(type = "string", format = "date-time", description = "Creation instant (ISO-8601 UTC)", example = "2026-06-10T09:15:30Z")
        Instant createdAt,

        @Schema(type = "string", format = "date-time", description = "Last update instant (ISO-8601 UTC)", example = "2026-06-10T09:15:30Z")
        Instant updatedAt
) {
}
