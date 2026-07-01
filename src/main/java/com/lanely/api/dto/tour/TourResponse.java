package com.lanely.api.dto.tour;

import com.lanely.api.dto.company.AddressDto;
import com.lanely.api.dto.geo.CoordinateDto;
import com.lanely.api.dto.geo.RouteInfoDto;
import com.lanely.api.dto.waybill.WaybillSummaryResponse;
import com.lanely.api.entity.enums.AccountType;
import com.lanely.api.entity.enums.TourStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Schema(name = "TourResponse", description = "A tour (round) with its assigned vehicle/driver, ordered waybills and computed route")
public record TourResponse(

        @Schema(description = "Tour identifier", example = "2b3c4d5e-6f70-8a9b-0c1d-2e3f4a5b6c7d")
        UUID id,

        @Schema(description = "Reference, unique within the company", example = "TUR-0001")
        String reference,

        @Schema(description = "Display name", example = "Rennes morning round")
        String name,

        @Schema(description = "Lifecycle status", example = "PLANNED")
        TourStatus status,

        @Schema(description = "Assigned driver account id, if any. May be a mobile driver profile or a web user.", nullable = true)
        UUID assignedAccountId,

        @Schema(description = "Type of the assigned driver account (PROFILE for a mobile driver, USER for a web user), if any",
                example = "PROFILE", nullable = true)
        AccountType assigneeType,

        @Schema(description = "Display name of the assigned driver (profile display name/username or user full name/email), if any",
                example = "John D.", nullable = true)
        String assigneeName,

        @Schema(description = "Assigned vehicle id, if any", nullable = true)
        UUID vehicleId,

        @Schema(description = "Depot postal address", nullable = true)
        AddressDto depot,

        @Schema(description = "Depot GPS coordinates", nullable = true)
        CoordinateDto depotLocation,

        @Schema(type = "string", format = "date", description = "Planned date (ISO-8601)", example = "2026-06-24", nullable = true)
        LocalDate plannedDate,

        @Schema(type = "string", format = "date-time", description = "Start instant (ISO-8601 UTC)", nullable = true)
        Instant startedAt,

        @Schema(type = "string", format = "date-time", description = "Completion instant (ISO-8601 UTC)", nullable = true)
        Instant completedAt,

        @Schema(description = "Cached full-tour route (depot -> stops -> depot)", nullable = true)
        RouteInfoDto route,

        @Schema(type = "string", format = "date-time", description = "Last optimization instant (ISO-8601 UTC)", nullable = true)
        Instant lastOptimizedAt,

        @Schema(description = "Ordered waybills (stops) of the tour")
        List<WaybillSummaryResponse> waybills,

        @Schema(description = "Free-form internal notes", nullable = true)
        String notes,

        @Schema(type = "string", format = "date-time", description = "Creation instant (ISO-8601 UTC)", example = "2026-06-10T09:15:30Z")
        Instant createdAt,

        @Schema(type = "string", format = "date-time", description = "Last update instant (ISO-8601 UTC)", example = "2026-06-10T09:15:30Z")
        Instant updatedAt
) {
}
