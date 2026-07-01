package com.lanely.api.dto.pricing;

import com.lanely.api.entity.enums.PricingBasis;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(name = "FuelSurchargeComponentDto", description = "An extra billable line applied when the fuel price crosses the threshold (THRESHOLD_COMPONENTS mode)")
public record FuelSurchargeComponentDto(

        @Schema(description = "Unique identifier of the surcharge component", example = "9f1c2d3e-4a5b-6c7d-8e9f-0a1b2c3d4e5f")
        UUID id,

        @Schema(description = "Display and breakdown order (ascending)", example = "1")
        int position,

        @Schema(description = "Human-readable label shown on the quote line", example = "Fuel surcharge (distance)")
        String label,

        @Schema(description = "Billing unit that drives the quantity for this surcharge line", example = "PER_KM")
        PricingBasis basis,

        @Schema(description = "Price charged per unit of the basis, tax-excluded (HT)", example = "0.1200")
        BigDecimal unitPrice,

        @Schema(description = "Free allowance subtracted before charging (in basis units)", example = "0.000", nullable = true)
        BigDecimal includedQuantity,

        @Schema(description = "Minimum billable quantity after the allowance", example = "0.000", nullable = true)
        BigDecimal minQuantity,

        @Schema(description = "Maximum billable quantity", example = "500.000", nullable = true)
        BigDecimal maxQuantity,

        @Schema(description = "Floor applied to the line total, tax-excluded (HT)", example = "0.00", nullable = true)
        BigDecimal minAmount,

        @Schema(description = "Ceiling applied to the line total, tax-excluded (HT)", example = "100.00", nullable = true)
        BigDecimal maxAmount
) {
}
