package com.lanely.api.integration.ors.dto;

public record OrsGeocodeResult(
        String label,
        String houseNumber,
        String street,
        String postcode,
        String city,
        Double lat,
        Double lon,
        String type,
        Integer distanceMeters,
        Double score
) {
}
