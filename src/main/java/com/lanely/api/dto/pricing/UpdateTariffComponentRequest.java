package com.lanely.api.dto.pricing;

import com.lanely.api.entity.enums.PricingBasis;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(name = "UpdateTariffComponentRequest", description = "Partial update of a rate card line. Only non-null fields are applied.")
public record UpdateTariffComponentRequest(

        @Schema(description = "Display and breakdown order (ascending)", example = "2", nullable = true)
        Integer position,

        @Schema(description = "Human-readable label shown on the quote line", example = "Distance", nullable = true)
        @Size(max = 120)
        String label,

        @Schema(description = "Billing unit that drives the quantity for this line", example = "PER_KM", nullable = true)
        PricingBasis basis,

        @Schema(description = "Price charged per unit of the basis (4 decimals), tax-excluded (HT)", example = "1.0500", nullable = true)
        @DecimalMin("0.0")
        BigDecimal unitPrice,

        @Schema(description = "Free allowance subtracted before charging (in basis units)", example = "10.000", nullable = true)
        @DecimalMin("0.0")
        BigDecimal includedQuantity,

        @Schema(description = "Minimum billable quantity after the allowance", example = "0.000", nullable = true)
        @DecimalMin("0.0")
        BigDecimal minQuantity,

        @Schema(description = "Maximum billable quantity", example = "500.000", nullable = true)
        @DecimalMin("0.0")
        BigDecimal maxQuantity,

        @Schema(description = "Floor applied to the line total, tax-excluded (HT)", example = "5.00", nullable = true)
        @DecimalMin("0.0")
        BigDecimal minAmount,

        @Schema(description = "Ceiling applied to the line total, tax-excluded (HT)", example = "250.00", nullable = true)
        @DecimalMin("0.0")
        BigDecimal maxAmount
) {
}
