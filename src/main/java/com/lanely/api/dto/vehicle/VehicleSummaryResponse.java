package com.lanely.api.dto.vehicle;

import com.lanely.api.entity.enums.VehicleStatus;
import com.lanely.api.entity.enums.VehicleType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(name = "VehicleSummaryResponse", description = "A vehicle as listed in a paginated collection (lightweight projection)")
public record VehicleSummaryResponse(

        @Schema(description = "Unique vehicle identifier", example = "9f1c2d3e-4a5b-6c7d-8e9f-0a1b2c3d4e5f")
        UUID id,

        @Schema(description = "Registration (license) plate", example = "AB-123-CD")
        String registrationPlate,

        @Schema(description = "Manufacturer (make)", example = "Renault", nullable = true)
        String make,

        @Schema(description = "Model", example = "Master", nullable = true)
        String model,

        @Schema(description = "Vehicle category", example = "TRUCK")
        VehicleType vehicleType,

        @Schema(description = "Lifecycle status", example = "ACTIVE")
        VehicleStatus status,

        @Schema(description = "Latest recorded mileage in kilometers", example = "84210", nullable = true)
        Integer latestMileageKm,

        @Schema(type = "string", format = "date-time", description = "Creation instant (ISO-8601 UTC)", example = "2026-06-10T09:15:30Z")
        Instant createdAt
) {
}
