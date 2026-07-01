package com.lanely.api.dto.vehicle;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(name = "MileageReadingResponse", description = "A mileage (odometer) reading recorded for a vehicle")
public record MileageReadingResponse(

        @Schema(description = "Unique reading identifier", example = "1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d")
        UUID id,

        @Schema(description = "Mileage value in kilometers", example = "84210")
        int valueKm,

        @Schema(type = "string", format = "date-time", description = "Instant the reading was taken (ISO-8601 UTC)", example = "2026-06-10T09:15:30Z")
        Instant recordedAt,

        @Schema(description = "Account id of the member who recorded the reading", example = "8a1f2c3b-4d5e-6f70-3f1c-8d2e9b4a4d6e", nullable = true)
        UUID recordedByUserId,

        @Schema(description = "Optional note about the reading", example = "Recorded at the depot before departure.", nullable = true)
        String note,

        @Schema(type = "string", format = "date-time", description = "Creation instant (ISO-8601 UTC)", example = "2026-06-10T09:15:30Z")
        Instant createdAt
) {
}
