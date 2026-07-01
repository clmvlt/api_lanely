package com.lanely.api.dto.geo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

@Schema(name = "CoordinateDto", description = "WGS84 geographic coordinates. Both fields must be provided together.")
public record CoordinateDto(

        @Schema(description = "Latitude (WGS84)", example = "48.1147", nullable = true)
        @DecimalMin("-90.0")
        @DecimalMax("90.0")
        Double latitude,

        @Schema(description = "Longitude (WGS84)", example = "-1.6794", nullable = true)
        @DecimalMin("-180.0")
        @DecimalMax("180.0")
        Double longitude
) {

    public boolean isComplete() {
        return latitude != null && longitude != null;
    }
}
