package com.lanely.api.dto.fuel;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(name = "RefreshFuelPricesResponse", description = "Outcome of a manual fuel price refresh from the government feed")
public record RefreshFuelPricesResponse(

        @Schema(description = "Identifier of the data source that was queried", example = "data.economie.gouv.fr")
        String source,

        @Schema(description = "Number of price observations created or updated by this refresh", example = "1")
        int ingested,

        @Schema(type = "string", format = "date-time", description = "Instant the refresh completed (ISO-8601 UTC)", example = "2026-06-23T06:00:00Z")
        Instant refreshedAt
) {
}
