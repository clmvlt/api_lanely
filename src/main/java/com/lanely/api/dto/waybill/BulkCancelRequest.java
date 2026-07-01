package com.lanely.api.dto.waybill;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

@Schema(name = "BulkCancelRequest", description = "Cancel (move to CANCELLED) several waybills at once. A failure on one item never fails "
        + "the whole batch; see BulkResultResponse for per-item outcomes.")
public record BulkCancelRequest(

        @Schema(description = "Identifiers of the waybills to cancel. Must be non-empty and contain at most 200 ids.",
                requiredMode = Schema.RequiredMode.REQUIRED,
                example = "[\"0a1b2c3d-4e5f-6a7b-8c9d-0e1f2a3b4c5d\",\"1b2c3d4e-5f6a-7b8c-9d0e-1f2a3b4c5d6e\"]")
        List<UUID> ids,

        @Schema(description = "Free-text note recorded on every produced status-history entry", example = "Order cancelled by customer", nullable = true)
        @Size(max = 2000)
        String note
) {
}
