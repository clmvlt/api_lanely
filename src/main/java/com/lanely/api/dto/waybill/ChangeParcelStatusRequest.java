package com.lanely.api.dto.waybill;

import com.lanely.api.entity.enums.ParcelStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(name = "ChangeParcelStatusRequest",
        description = "Set a parcel (goods line) to a new status. Any status can be set from any other (no lifecycle "
                + "restriction), including moving backward or out of a terminal status. "
                + "Moving to AT_DOCK stamps the parcel dock-entry instant and clears its dock-exit instant; "
                + "moving away from AT_DOCK stamps its dock-exit instant.")
public record ChangeParcelStatusRequest(

        @Schema(description = "Target status. One of PENDING, LOADED, AT_DOCK, IN_TRANSIT, DELIVERED, FAILED, CANCELLED.",
                example = "AT_DOCK", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        ParcelStatus status,

        @Schema(description = "Free-text note for this change, e.g. an anomaly or delivery issue",
                example = "Package damaged on arrival", nullable = true)
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
