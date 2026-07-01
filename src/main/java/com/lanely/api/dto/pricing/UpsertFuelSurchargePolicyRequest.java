package com.lanely.api.dto.pricing;

import com.lanely.api.entity.enums.FuelSurchargeMode;
import com.lanely.api.entity.enums.FuelType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

@Schema(name = "UpsertFuelSurchargePolicyRequest", description = "Create or replace the fuel indexation policy of a rate card (tariff)")
public record UpsertFuelSurchargePolicyRequest(

        @Schema(description = "Whether the fuel surcharge is applied to quotes", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        Boolean enabled,

        @Schema(description = "Fuel type whose index drives the surcharge", example = "DIESEL", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        FuelType fuelType,

        @Schema(description = "Surcharge computation mode. THRESHOLD_COMPONENTS adds the surchargeComponents lines when the current "
                + "price exceeds thresholdPrice; INDEXED_PERCENT applies dieselShareRatio x (current - reference)/reference as a "
                + "percentage of the subtotal.", example = "THRESHOLD_COMPONENTS", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        FuelSurchargeMode mode,

        @Schema(description = "Threshold price per liter above which the surcharge applies (required for THRESHOLD_COMPONENTS)", example = "1.7000", nullable = true)
        @DecimalMin("0.0")
        BigDecimal thresholdPrice,

        @Schema(description = "Reference price per liter baseline (required for INDEXED_PERCENT)", example = "1.5000", nullable = true)
        @DecimalMin("0.0")
        BigDecimal referencePrice,

        @Schema(description = "Diesel share of the cost price as a ratio between 0 and 1 (required for INDEXED_PERCENT)", example = "0.3000", nullable = true)
        @DecimalMin("0.0")
        @DecimalMax("1.0")
        BigDecimal dieselShareRatio,

        @Schema(description = "Whether a negative indexed surcharge (price drop) is floored at zero. Defaults to true.", example = "true", nullable = true)
        Boolean clampAtZero,

        @Schema(description = "Optional data source to pin the index lookup to. Omit to use the latest from any source.", example = "data.economie.gouv.fr", nullable = true)
        @Size(max = 64)
        String sourceFilter,

        @Schema(description = "Extra lines applied above the threshold (used by THRESHOLD_COMPONENTS mode)", nullable = true)
        @Valid
        List<FuelSurchargeComponentRequest> surchargeComponents
) {
}
