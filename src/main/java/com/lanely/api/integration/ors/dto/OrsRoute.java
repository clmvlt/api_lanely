package com.lanely.api.integration.ors.dto;

import java.time.Instant;
import java.util.List;

public record OrsRoute(
        String vehicleId,
        Instant departureTime,
        Instant returnTime,
        Long drivingTimeSeconds,
        Long serviceTimeSeconds,
        Long distanceMeters,
        Integer totalDemand,
        List<OrsStop> stops,
        String geometryPolyline
) {
}
