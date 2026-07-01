package com.lanely.api.dto.tour;

import com.lanely.api.dto.geo.SkippedVisitDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "TourOptimizeResponse", description = "Result of optimizing a tour: the updated tour (with the new stop order and route) and any skipped stops")
public record TourOptimizeResponse(

        @Schema(description = "The tour after applying the optimal order")
        TourResponse tour,

        @Schema(description = "Stops excluded from optimization because they could not be attached to the road network. "
                + "These keep their previous position and must be reviewed.")
        List<SkippedVisitDto> skippedVisits
) {
}
