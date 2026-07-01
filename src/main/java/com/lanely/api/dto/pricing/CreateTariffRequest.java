package com.lanely.api.dto.pricing;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.UUID;

@Schema(name = "CreateTariffRequest", description = "Payload to create a rate card (tariff) for a transport company")
public record CreateTariffRequest(

        @Schema(description = "Display name of the rate card", example = "Standard national 2026", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        @Size(max = 120)
        String name,

        @Schema(description = "Free-form description", example = "Default grid applied to national deliveries", nullable = true)
        String description,

        @Schema(description = "ISO 4217 currency code. Defaults to EUR.", example = "EUR", nullable = true)
        @Size(min = 3, max = 3)
        String currency,

        @Schema(description = "Whether this is the company default grid (fallback when no client-specific grid matches). "
                + "Only one ACTIVE default is allowed per company, and a client-bound grid cannot be default.", example = "true", nullable = true)
        Boolean isDefault,

        @Schema(description = "Optional client this grid is specific to. Null makes it a company-wide grid.", example = "7a1c2d3e-4a5b-6c7d-8e9f-0a1b2c3d4e5f", nullable = true)
        UUID clientId,

        @Schema(type = "string", format = "date", description = "Start of the validity window (inclusive, civil date). Null means no lower bound.", example = "2026-01-01", nullable = true)
        LocalDate validFrom,

        @Schema(type = "string", format = "date", description = "End of the validity window (inclusive, civil date). Null means no upper bound.", example = "2026-12-31", nullable = true)
        LocalDate validUntil,

        @Schema(description = "Rounding mode applied to the final total. Defaults to HALF_UP.", example = "HALF_UP", nullable = true)
        RoundingMode roundingMode,

        @Schema(description = "Number of decimals kept on the final total. Defaults to 2.", example = "2", nullable = true)
        @Min(0)
        @Max(6)
        Integer roundingScale,

        @Schema(description = "Minimum charge (minimum de perception) applied as a floor to the total, tax-excluded (HT)", example = "15.00", nullable = true)
        @DecimalMin("0.0")
        BigDecimal minChargeAmount
) {
}
