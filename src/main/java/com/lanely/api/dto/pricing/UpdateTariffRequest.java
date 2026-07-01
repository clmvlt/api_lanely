package com.lanely.api.dto.pricing;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.UUID;

@Schema(name = "UpdateTariffRequest", description = "Partial update of a rate card. Only non-null fields are applied.")
public record UpdateTariffRequest(

        @Schema(description = "Display name of the rate card", example = "Standard national 2026", nullable = true)
        @Size(max = 120)
        String name,

        @Schema(description = "Free-form description", example = "Updated grid", nullable = true)
        String description,

        @Schema(description = "ISO 4217 currency code", example = "EUR", nullable = true)
        @Size(min = 3, max = 3)
        String currency,

        @Schema(description = "Whether this is the company default grid", example = "true", nullable = true)
        Boolean isDefault,

        @Schema(description = "Client this grid is specific to. Provide the special all-zero UUID to detach (make company-wide).", example = "7a1c2d3e-4a5b-6c7d-8e9f-0a1b2c3d4e5f", nullable = true)
        UUID clientId,

        @Schema(type = "string", format = "date", description = "Start of the validity window (inclusive, civil date)", example = "2026-01-01", nullable = true)
        LocalDate validFrom,

        @Schema(type = "string", format = "date", description = "End of the validity window (inclusive, civil date)", example = "2026-12-31", nullable = true)
        LocalDate validUntil,

        @Schema(description = "Rounding mode applied to the final total", example = "HALF_UP", nullable = true)
        RoundingMode roundingMode,

        @Schema(description = "Number of decimals kept on the final total", example = "2", nullable = true)
        @Min(0)
        @Max(6)
        Integer roundingScale,

        @Schema(description = "Minimum charge applied as a floor to the total, tax-excluded (HT)", example = "15.00", nullable = true)
        @DecimalMin("0.0")
        BigDecimal minChargeAmount
) {
}
