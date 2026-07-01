package com.lanely.api.integration.ors.dto;

import java.time.Instant;

public record OrsStop(
        String visitId,
        String name,
        Double lat,
        Double lon,
        Long cumulativeDistanceMeters,
        Long cumulativeDrivingSeconds,
        Instant arrivalTime,
        Instant departureTime,
        Integer demand
) {
}
