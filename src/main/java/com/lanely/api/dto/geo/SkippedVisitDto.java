package com.lanely.api.dto.geo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "SkippedVisitDto", description = "A visit excluded from optimization because it could not be attached to the road network")
public record SkippedVisitDto(

        @Schema(description = "Identifier of the skipped point", example = "A")
        String visitId,

        @Schema(description = "Display name", example = "Client A", nullable = true)
        String name,

        @Schema(description = "Reason: UNROUTABLE (no road) or TOO_FAR (nearest road beyond the snap threshold)", example = "TOO_FAR")
        String reason,

        @Schema(description = "Distance in meters to the nearest road", example = "1830", nullable = true)
        Double snapDistanceMeters
) {
}
