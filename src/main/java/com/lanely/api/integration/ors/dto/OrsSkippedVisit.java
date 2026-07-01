package com.lanely.api.integration.ors.dto;

public record OrsSkippedVisit(
        String visitId,
        String name,
        Double lat,
        Double lon,
        String reason,
        Double snapDistanceMeters
) {
}
