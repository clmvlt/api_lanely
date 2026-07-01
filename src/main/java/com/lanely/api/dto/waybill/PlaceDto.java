package com.lanely.api.dto.waybill;

import com.lanely.api.dto.company.AddressDto;
import com.lanely.api.dto.geo.CoordinateDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;

import java.time.Instant;
import java.util.UUID;

@Schema(name = "PlaceDto", description = "A place of taking over or delivery. It can be a free address (with optional GPS), linked to an existing "
        + "client (set clientId, and optionally clientAddressId to reuse one of its stored addresses), or linked to a client while overriding "
        + "the address (set clientId plus a custom address). When no GPS coordinates are provided and none can be derived from the linked "
        + "client address, the server geocodes the resolved address and fails with 400 when it cannot be located.")
public record PlaceDto(

        @Schema(description = "Postal address. Overrides the linked client address when both are provided.", nullable = true)
        @Valid
        AddressDto address,

        @Schema(description = "GPS coordinates (used for routing). When omitted, the server derives them from the linked client address or geocodes the address.", nullable = true)
        @Valid
        CoordinateDto location,

        @Schema(description = "Optional link to an existing client for this point", example = "9f1c2d3e-4a5b-6c7d-8e9f-0a1b2c3d4e5f", nullable = true)
        UUID clientId,

        @Schema(description = "Optional link to a specific address of the linked client. Requires clientId.", example = "1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d", nullable = true)
        UUID clientAddressId,

        @Schema(type = "string", format = "date-time", description = "Planned date/time (ISO-8601 UTC)", example = "2026-06-23T08:30:00Z", nullable = true)
        Instant plannedAt
) {
}
