package com.lanely.api.integration.ors.dto;

import java.time.Instant;
import java.util.List;

public record OrsOptimizeRequest(
        OrsCoordinate depot,
        Integer vehicleCount,
        Integer vehicleCapacity,
        Instant departureTime,
        Boolean includeGeometry,
        String geometryFormat,
        List<OrsVisit> visits
) {
}
