package com.lanely.api.dto.waybill;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

@Schema(name = "RouteInputDto",
        description = "Optional client-supplied round-trip route (company depot -> shipper pickup -> consignee delivery -> company depot). "
                + "When provided, the API stores these values as-is and does NOT compute the route itself. "
                + "When omitted (or null), the API computes the route from the depot and place coordinates. "
                + "Partial input is accepted: only the supplied fields are stored.")
public record RouteInputDto(

        @Schema(description = "Total driving distance in meters", example = "184300", nullable = true)
        @PositiveOrZero
        Long distanceMeters,

        @Schema(description = "Total driving duration in seconds", example = "12450", nullable = true)
        @PositiveOrZero
        Long durationSeconds,

        @Schema(description = "Encoded polyline of the full route (Google/OSRM precision 5, lat/lon order)",
                example = "ydlrHnwfA~A_@dGsT", nullable = true)
        @Size(max = 100000)
        String geometryPolyline
) {

    public boolean hasAnyValue() {
        return distanceMeters != null || durationSeconds != null
                || (geometryPolyline != null && !geometryPolyline.isBlank());
    }
}
