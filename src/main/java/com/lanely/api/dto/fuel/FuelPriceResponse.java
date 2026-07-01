package com.lanely.api.dto.fuel;

import com.lanely.api.entity.enums.FuelType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Schema(name = "FuelPriceResponse", description = "A fuel price observation ingested from a government open-data feed")
public record FuelPriceResponse(

        @Schema(description = "Unique identifier of the price observation", example = "9f1c2d3e-4a5b-6c7d-8e9f-0a1b2c3d4e5f")
        UUID id,

        @Schema(description = "Fuel type the price refers to", example = "DIESEL")
        FuelType fuelType,

        @Schema(description = "Average price in currency units per liter", example = "1.7820")
        BigDecimal price,

        @Schema(description = "ISO 4217 currency code", example = "EUR")
        String currency,

        @Schema(type = "string", format = "date", description = "Reference (civil) date of the price period (ISO-8601)", example = "2026-06-22")
        LocalDate referenceDate,

        @Schema(description = "Identifier of the data source the observation came from", example = "data.economie.gouv.fr")
        String source,

        @Schema(type = "string", format = "date-time", description = "Instant the observation was ingested (ISO-8601 UTC)", example = "2026-06-23T06:00:00Z")
        Instant fetchedAt
) {
}
