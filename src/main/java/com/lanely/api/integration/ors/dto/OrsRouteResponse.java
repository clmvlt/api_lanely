package com.lanely.api.integration.ors.dto;

public record OrsRouteResponse(
        Long distanceMeters,
        Long durationSeconds,
        String geometryFormat,
        String geometryPolyline
) {
}
