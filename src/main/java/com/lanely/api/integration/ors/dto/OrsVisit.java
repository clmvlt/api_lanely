package com.lanely.api.integration.ors.dto;

public record OrsVisit(
        String id,
        String name,
        Double lat,
        Double lon,
        Integer demand,
        Integer serviceDurationSeconds
) {
}
