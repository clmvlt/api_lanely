package com.lanely.api.dto.geo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(name = "RouteInfoDto", description = "A computed road route: total distance, duration and encoded geometry. Null fields mean the route has not been computed yet.")
public record RouteInfoDto(

        @Schema(description = "Total driving distance in meters", example = "184300", nullable = true)
        Long distanceMeters,

        @Schema(description = "Total driving duration in seconds", example = "12450", nullable = true)
        Long durationSeconds,

        @Schema(description = "Encoded polyline of the full route (Google/OSRM precision 5, lat/lon order)", example = "ydlrHnwfA~A_@dGsT", nullable = true)
        String geometryPolyline,

        @Schema(type = "string", format = "date-time", description = "Instant the route was computed (ISO-8601 UTC)", example = "2026-06-10T09:15:30Z", nullable = true)
        Instant computedAt
) {
}
