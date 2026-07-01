package com.lanely.api.dto.tour;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

@Schema(name = "RoutePreviewResponse", description = "Computed route for a previewed stop order (not persisted)")
public record RoutePreviewResponse(

        @Schema(description = "Echo of the previewed order (depot is implicit at both ends)")
        List<UUID> orderedWaybillIds,

        @Schema(description = "Total driving distance in meters", example = "184300", nullable = true)
        Long distanceMeters,

        @Schema(description = "Total driving duration in seconds", example = "12450", nullable = true)
        Long durationSeconds,

        @Schema(description = "Encoded polyline of the full route (depot -> stops -> depot)", example = "ydlrHnwfA~A_@dGsT", nullable = true)
        String geometryPolyline
) {
}
