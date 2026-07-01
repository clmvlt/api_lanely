package com.lanely.api.dto.pricing;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;

@Schema(name = "QuoteInputsDto", description = "Ad-hoc quantities used to price a delivery without referencing an existing waybill. "
        + "Missing values default to zero (or one stop / one waybill).")
public record QuoteInputsDto(

        @Schema(description = "Distance in kilometers", example = "120.500", nullable = true)
        @DecimalMin("0.0")
        BigDecimal distanceKm,

        @Schema(description = "Total gross weight in kilograms", example = "850.000", nullable = true)
        @DecimalMin("0.0")
        BigDecimal totalWeightKg,

        @Schema(description = "Total volume in cubic meters", example = "3.500", nullable = true)
        @DecimalMin("0.0")
        BigDecimal totalVolumeM3,

        @Schema(description = "Number of packages", example = "12", nullable = true)
        @Min(0)
        Integer packageCount,

        @Schema(description = "Number of delivery points / stops", example = "1", nullable = true)
        @Min(0)
        Integer stopCount
) {
}
