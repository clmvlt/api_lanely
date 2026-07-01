package com.lanely.api.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(name = "StatusHistoryEntry",
        description = "A single status-change entry recording the transition, who performed it, when, and optional context")
public record StatusHistoryResponse(

        @Schema(description = "History entry identifier", example = "3a4b5c6d-7e8f-9a0b-1c2d-3e4f5a6b7c8d")
        UUID id,

        @Schema(description = "Previous status; null for the initial entry created with the resource",
                example = "ISSUED", nullable = true)
        String fromStatus,

        @Schema(description = "New status after the change", example = "COLLECTED")
        String toStatus,

        @Schema(description = "Account that performed the change")
        StatusActorDto actor,

        @Schema(description = "Free-text note attached to the change (e.g. an anomaly or delivery issue)",
                example = "Recipient absent, parcel left at the front desk", nullable = true)
        String note,

        @Schema(description = "Latitude where the change happened (WGS84), when captured", example = "48.8566", nullable = true)
        Double latitude,

        @Schema(description = "Longitude where the change happened (WGS84), when captured", example = "2.3522", nullable = true)
        Double longitude,

        @Schema(type = "string", format = "date-time", description = "Instant of the change (ISO-8601 UTC)",
                example = "2026-06-10T09:15:30Z")
        Instant changedAt
) {
}
