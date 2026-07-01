package com.lanely.api.dto.pricing;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Schema(name = "QuoteResponse", description = "A computed price quote with its full breakdown. All monetary amounts are tax-excluded (HT).")
public record QuoteResponse(

        @Schema(description = "Rate card used for the computation", example = "9f1c2d3e-4a5b-6c7d-8e9f-0a1b2c3d4e5f")
        UUID tariffId,

        @Schema(description = "Name of the rate card used", example = "Standard national 2026")
        String tariffName,

        @Schema(description = "ISO 4217 currency of the quote", example = "EUR")
        String currency,

        @Schema(description = "Breakdown lines (base lines then surcharge lines)")
        List<QuoteLineDto> lines,

        @Schema(description = "Sum of the base lines before fuel surcharge, tax-excluded (HT)", example = "114.48")
        BigDecimal subtotal,

        @Schema(description = "Total of the fuel surcharge lines, tax-excluded (HT)", example = "12.05")
        BigDecimal surchargeTotal,

        @Schema(description = "Final total after the minimum charge floor and rounding, tax-excluded (HT)", example = "126.53")
        BigDecimal total,

        @Schema(description = "Fuel price per liter used for the surcharge, or null when no index was available or no policy applied", example = "1.7820", nullable = true)
        BigDecimal fuelPriceUsed,

        @Schema(type = "string", format = "date", description = "Reference date of the fuel price used, or null", example = "2026-06-22", nullable = true)
        LocalDate fuelReferenceDate,

        @Schema(description = "Non-fatal notes about the computation (e.g. route distance missing, no fuel index available)",
                example = "[\"error.pricing.route-not-computed\"]")
        List<String> warnings
) {
}
