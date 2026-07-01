package com.lanely.api.dto.pricing;

import com.lanely.api.entity.enums.ComponentKind;
import com.lanely.api.entity.enums.PricingBasis;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(name = "TariffComponentDto", description = "A single billable line of a rate card (tariff)")
public record TariffComponentDto(

        @Schema(description = "Unique identifier of the component", example = "9f1c2d3e-4a5b-6c7d-8e9f-0a1b2c3d4e5f")
        UUID id,

        @Schema(description = "Display order and breakdown order (ascending)", example = "1")
        int position,

        @Schema(description = "Human-readable label shown on the quote line", example = "Distance")
        String label,

        @Schema(description = "Billing unit that drives the quantity for this line", example = "PER_KM")
        PricingBasis basis,

        @Schema(description = "BASE for a normal line, SURCHARGE for an add-on line", example = "BASE")
        ComponentKind kind,

        @Schema(description = "Price charged per unit of the basis, tax-excluded (HT)", example = "0.9500")
        BigDecimal unitPrice,

        @Schema(description = "Free allowance subtracted before charging (in basis units)", example = "10.000", nullable = true)
        BigDecimal includedQuantity,

        @Schema(description = "Minimum billable quantity after the allowance (in basis units)", example = "0.000", nullable = true)
        BigDecimal minQuantity,

        @Schema(description = "Maximum billable quantity (in basis units)", example = "500.000", nullable = true)
        BigDecimal maxQuantity,

        @Schema(description = "Floor applied to the line total, tax-excluded (HT)", example = "5.00", nullable = true)
        BigDecimal minAmount,

        @Schema(description = "Ceiling applied to the line total, tax-excluded (HT)", example = "250.00", nullable = true)
        BigDecimal maxAmount
) {
}
