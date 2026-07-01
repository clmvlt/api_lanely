package com.lanely.api.integration.ors.dto;

import java.util.List;

public record OrsRouteRequest(
        OrsCoordinate from,
        OrsCoordinate to,
        List<OrsCoordinate> points,
        String geometryFormat
) {

    public static OrsRouteRequest ordered(List<OrsCoordinate> points) {
        return new OrsRouteRequest(null, null, points, "POLYLINE");
    }
}
