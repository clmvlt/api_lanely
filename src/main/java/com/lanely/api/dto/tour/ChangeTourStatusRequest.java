package com.lanely.api.dto.tour;

import com.lanely.api.entity.enums.TourStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(name = "ChangeTourStatusRequest", description = "Move a tour to a new status. Allowed transitions follow the tour lifecycle.")
public record ChangeTourStatusRequest(

        @Schema(description = "Target status", example = "IN_PROGRESS", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        TourStatus status,

        @Schema(description = "Free-text note recorded on the status-history entry, e.g. an anomaly",
                example = "Started 30 min late due to traffic", nullable = true)
        @Size(max = 2000)
        String note,

        @Schema(description = "Latitude where the change happened (WGS84), optional", example = "48.8566", nullable = true)
        @DecimalMin("-90.0")
        @DecimalMax("90.0")
        Double latitude,

        @Schema(description = "Longitude where the change happened (WGS84), optional", example = "2.3522", nullable = true)
        @DecimalMin("-180.0")
        @DecimalMax("180.0")
        Double longitude
) {
}
