package com.lanely.api.dto.pricing;

import com.lanely.api.entity.enums.ComponentKind;
import com.lanely.api.entity.enums.PricingBasis;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(name = "QuoteLineDto", description = "A single computed line of a price quote breakdown")
public record QuoteLineDto(

        @Schema(description = "Identifier of the source component, or null for a computed surcharge line", example = "9f1c2d3e-4a5b-6c7d-8e9f-0a1b2c3d4e5f", nullable = true)
        UUID componentId,

        @Schema(description = "Human-readable label of the line", example = "Distance")
        String label,

        @Schema(description = "Billing unit used for the line", example = "PER_KM")
        PricingBasis basis,

        @Schema(description = "BASE for a normal line, SURCHARGE for a fuel/add-on line", example = "BASE")
        ComponentKind kind,

        @Schema(description = "Unit price applied, tax-excluded (HT)", example = "0.9500")
        BigDecimal unitPrice,

        @Schema(description = "Billable quantity after allowances and clamps", example = "120.500")
        BigDecimal quantity,

        @Schema(description = "Computed line total, tax-excluded (HT)", example = "114.48")
        BigDecimal lineTotal
) {
}
