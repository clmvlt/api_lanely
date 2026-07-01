package com.lanely.api.dto.vehicle;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

@Schema(name = "CreateMileageReadingRequest", description = "Payload to add a mileage (odometer) reading to a vehicle")
public record CreateMileageReadingRequest(

        @Schema(description = "Mileage value in kilometers", example = "84210", requiredMode = Schema.RequiredMode.REQUIRED, minimum = "0")
        @NotNull
        @Min(0)
        Integer valueKm,

        @Schema(type = "string", format = "date-time", description = "Instant the reading was taken (ISO-8601 UTC)", example = "2026-06-10T09:15:30Z", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        Instant recordedAt,

        @Schema(description = "Optional note about the reading", example = "Recorded at the depot before departure.", nullable = true)
        String note
) {
}
