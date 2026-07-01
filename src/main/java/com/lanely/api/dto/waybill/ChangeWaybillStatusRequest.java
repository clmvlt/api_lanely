package com.lanely.api.dto.waybill;

import com.lanely.api.entity.enums.WaybillStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(name = "ChangeWaybillStatusRequest", description = "Set a waybill to a new status. Any status can be set from any other "
        + "(no lifecycle restriction), including moving backward or out of a terminal status. "
        + "Moving to AT_DOCK stamps the dock-entry instant (dockEnteredAt) and clears the dock-exit instant; "
        + "moving away from AT_DOCK stamps the dock-exit instant (dockExitedAt).")
public record ChangeWaybillStatusRequest(

        @Schema(description = "Target status. One of DRAFT, ISSUED, COLLECTED, AT_DOCK, IN_TRANSIT, DELIVERED, FAILED, CANCELLED.",
                example = "AT_DOCK", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        WaybillStatus status,

        @Schema(description = "Reason, required when moving to FAILED", example = "Recipient absent, no safe place", nullable = true)
        @Size(max = 1000)
        String failureReason,

        @Schema(description = "Free-text note recorded on the status-history entry, e.g. an anomaly",
                example = "Two pallets refused by the recipient", nullable = true)
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
