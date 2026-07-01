package com.lanely.api.integration.ors.dto;

import java.util.List;

public record OrsOptimizeResponse(
        String score,
        Long totalDrivingTimeSeconds,
        Long totalDistanceMeters,
        List<OrsRoute> routes,
        List<OrsSkippedVisit> skippedVisits
) {
}
