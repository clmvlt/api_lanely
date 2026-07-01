package com.lanely.api.dto.waybill;

import com.lanely.api.dto.company.AddressDto;
import com.lanely.api.dto.geo.CoordinateDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(name = "PlaceResponse", description = "A place of taking over or delivery")
public record PlaceResponse(

        @Schema(description = "Postal address", nullable = true)
        AddressDto address,

        @Schema(description = "GPS coordinates", nullable = true)
        CoordinateDto location,

        @Schema(description = "Linked client id, if any", nullable = true)
        UUID clientId,

        @Schema(description = "Linked client address id, if any", nullable = true)
        UUID clientAddressId,

        @Schema(type = "string", format = "date-time", description = "Planned date/time (ISO-8601 UTC)", example = "2026-06-23T08:30:00Z", nullable = true)
        Instant plannedAt,

        @Schema(type = "string", format = "date-time", description = "Actual date/time (ISO-8601 UTC)", example = "2026-06-23T08:42:00Z", nullable = true)
        Instant actualAt
) {
}
