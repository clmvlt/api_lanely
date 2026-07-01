package com.lanely.api.dto.waybill;

import com.lanely.api.entity.enums.WaybillStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

@Schema(name = "BulkStatusRequest", description = "Change the status of several waybills at once. The same transition rules as the "
        + "single-waybill status endpoint apply per item: any status can be set from any other (no lifecycle restriction). A failure on "
        + "one item never fails the whole batch; see BulkResultResponse for per-item outcomes.")
public record BulkStatusRequest(

        @Schema(description = "Identifiers of the waybills to update. Must be non-empty and contain at most 200 ids. Ids that do not "
                + "belong to the company are reported per item with code WAYBILL_NOT_FOUND.",
                requiredMode = Schema.RequiredMode.REQUIRED,
                example = "[\"0a1b2c3d-4e5f-6a7b-8c9d-0e1f2a3b4c5d\",\"1b2c3d4e-5f6a-7b8c-9d0e-1f2a3b4c5d6e\"]")
        List<UUID> ids,

        @Schema(description = "Target status applied to every listed waybill", example = "IN_TRANSIT",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        WaybillStatus status,

        @Schema(description = "Reason applied to each item; required (per item) when status is FAILED, ignored otherwise",
                example = "Recipient absent, no safe place", nullable = true)
        @Size(max = 1000)
        String failureReason,

        @Schema(description = "Free-text note recorded on every produced status-history entry", example = "Bulk dispatch", nullable = true)
        @Size(max = 2000)
        String note
) {
}
