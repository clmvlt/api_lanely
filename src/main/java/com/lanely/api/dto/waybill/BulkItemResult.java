package com.lanely.api.dto.waybill;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(name = "BulkItemResult", description = "Outcome of a single waybill within a bulk operation")
public record BulkItemResult(

        @Schema(description = "Identifier of the waybill this outcome refers to", example = "0a1b2c3d-4e5f-6a7b-8c9d-0e1f2a3b4c5d")
        UUID id,

        @Schema(description = "OK when the operation succeeded for this item, ERROR otherwise", example = "OK")
        BulkItemStatus status,

        @Schema(description = "Error detail, present only when status is ERROR", nullable = true)
        BulkItemError error
) {

    public static BulkItemResult ok(UUID id) {
        return new BulkItemResult(id, BulkItemStatus.OK, null);
    }

    public static BulkItemResult error(UUID id, String code, String message) {
        return new BulkItemResult(id, BulkItemStatus.ERROR, new BulkItemError(code, message));
    }
}
