package com.lanely.api.dto.tour;

import com.lanely.api.entity.enums.AccountType;
import com.lanely.api.entity.enums.TourStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Schema(name = "TourSummaryResponse", description = "Compact view of a tour for lists")
public record TourSummaryResponse(

        @Schema(description = "Tour identifier", example = "2b3c4d5e-6f70-8a9b-0c1d-2e3f4a5b6c7d")
        UUID id,

        @Schema(description = "Reference", example = "TUR-0001")
        String reference,

        @Schema(description = "Display name", example = "Rennes morning round")
        String name,

        @Schema(description = "Lifecycle status", example = "PLANNED")
        TourStatus status,

        @Schema(description = "Assigned driver account id, if any. May be a mobile driver profile or a web user.", nullable = true)
        UUID assignedAccountId,

        @Schema(description = "Type of the assigned driver account (PROFILE or USER), if any", example = "PROFILE", nullable = true)
        AccountType assigneeType,

        @Schema(description = "Display name of the assigned driver, if any", example = "John D.", nullable = true)
        String assigneeName,

        @Schema(description = "Assigned vehicle id, if any", nullable = true)
        UUID vehicleId,

        @Schema(type = "string", format = "date", description = "Planned date (ISO-8601)", example = "2026-06-24", nullable = true)
        LocalDate plannedDate,

        @Schema(description = "Total driving distance in meters, if computed", example = "184300", nullable = true)
        Long distanceMeters,

        @Schema(description = "Total driving duration in seconds, if computed", example = "12450", nullable = true)
        Long durationSeconds,

        @Schema(type = "string", format = "date-time", description = "Creation instant (ISO-8601 UTC)", example = "2026-06-10T09:15:30Z")
        Instant createdAt
) {
}
