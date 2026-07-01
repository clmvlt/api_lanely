package com.lanely.api.dto.pricing;

import com.lanely.api.entity.enums.FuelSurchargeMode;
import com.lanely.api.entity.enums.FuelType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Schema(name = "FuelSurchargePolicyResponse", description = "The fuel indexation policy attached to a rate card (tariff)")
public record FuelSurchargePolicyResponse(

        @Schema(description = "Unique identifier of the policy", example = "9f1c2d3e-4a5b-6c7d-8e9f-0a1b2c3d4e5f")
        UUID id,

        @Schema(description = "Whether the fuel surcharge is currently applied to quotes", example = "true")
        boolean enabled,

        @Schema(description = "Fuel type whose index drives the surcharge", example = "DIESEL")
        FuelType fuelType,

        @Schema(description = "Surcharge computation mode. THRESHOLD_COMPONENTS adds extra lines above the threshold; "
                + "INDEXED_PERCENT applies a percentage of the subtotal based on the index variation.", example = "THRESHOLD_COMPONENTS")
        FuelSurchargeMode mode,

        @Schema(description = "Threshold price per liter above which the surcharge applies (THRESHOLD_COMPONENTS)", example = "1.7000", nullable = true)
        BigDecimal thresholdPrice,

        @Schema(description = "Reference price per liter used as the baseline (INDEXED_PERCENT)", example = "1.5000", nullable = true)
        BigDecimal referencePrice,

        @Schema(description = "Diesel share of the cost price as a ratio between 0 and 1 (INDEXED_PERCENT)", example = "0.3000", nullable = true)
        BigDecimal dieselShareRatio,

        @Schema(description = "Whether a negative indexed surcharge (price drop) is floored at zero", example = "true")
        boolean clampAtZero,

        @Schema(description = "Optional data source to pin the index lookup to", example = "data.economie.gouv.fr", nullable = true)
        String sourceFilter,

        @Schema(description = "Extra lines applied above the threshold (THRESHOLD_COMPONENTS mode)")
        List<FuelSurchargeComponentDto> surchargeComponents
) {
}
