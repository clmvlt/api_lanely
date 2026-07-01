package com.lanely.api.dto.pricing;

import com.lanely.api.entity.enums.PricingBasis;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(name = "FuelSurchargeComponentRequest", description = "An extra billable line to apply above the fuel threshold (THRESHOLD_COMPONENTS mode)")
public record FuelSurchargeComponentRequest(

        @Schema(description = "Display and breakdown order (ascending). Defaults to list order when omitted.", example = "1", nullable = true)
        Integer position,

        @Schema(description = "Human-readable label shown on the quote line", example = "Fuel surcharge (distance)", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        @Size(max = 120)
        String label,

        @Schema(description = "Billing unit that drives the quantity for this surcharge line", example = "PER_KM", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        PricingBasis basis,

        @Schema(description = "Price charged per unit of the basis (4 decimals), tax-excluded (HT)", example = "0.1200", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        @DecimalMin("0.0")
        BigDecimal unitPrice,

        @Schema(description = "Free allowance subtracted before charging (in basis units)", example = "0.000", nullable = true)
        @DecimalMin("0.0")
        BigDecimal includedQuantity,

        @Schema(description = "Minimum billable quantity after the allowance", example = "0.000", nullable = true)
        @DecimalMin("0.0")
        BigDecimal minQuantity,

        @Schema(description = "Maximum billable quantity", example = "500.000", nullable = true)
        @DecimalMin("0.0")
        BigDecimal maxQuantity,

        @Schema(description = "Floor applied to the line total, tax-excluded (HT)", example = "0.00", nullable = true)
        @DecimalMin("0.0")
        BigDecimal minAmount,

        @Schema(description = "Ceiling applied to the line total, tax-excluded (HT)", example = "100.00", nullable = true)
        @DecimalMin("0.0")
        BigDecimal maxAmount
) {
}
