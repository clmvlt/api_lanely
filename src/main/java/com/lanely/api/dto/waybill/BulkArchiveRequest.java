package com.lanely.api.dto.waybill;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

@Schema(name = "BulkArchiveRequest", description = "Archive or unarchive several waybills at once. Archiving is idempotent and does not "
        + "change the business status. A failure on one item never fails the whole batch; see BulkResultResponse for per-item outcomes.")
public record BulkArchiveRequest(

        @Schema(description = "Identifiers of the waybills to (un)archive. Must be non-empty and contain at most 200 ids.",
                requiredMode = Schema.RequiredMode.REQUIRED,
                example = "[\"0a1b2c3d-4e5f-6a7b-8c9d-0e1f2a3b4c5d\",\"1b2c3d4e-5f6a-7b8c-9d0e-1f2a3b4c5d6e\"]")
        List<UUID> ids,

        @Schema(description = "true to archive the listed waybills, false to unarchive them", example = "true",
                requiredMode = Schema.RequiredMode.REQUIRED)
        boolean archived
) {
}
