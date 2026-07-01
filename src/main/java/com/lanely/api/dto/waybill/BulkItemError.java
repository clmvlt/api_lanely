package com.lanely.api.dto.waybill;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "BulkItemError", description = "Error detail for a failed bulk item")
public record BulkItemError(

        @Schema(description = "Stable, machine-readable error code (English identifier, not localized). One of: WAYBILL_NOT_FOUND, "
                + "INVALID_TRANSITION, FAILURE_REASON_REQUIRED.", example = "WAYBILL_NOT_FOUND")
        String code,

        @Schema(description = "Human-readable, localized message (resolved from the Accept-Language header)", example = "Waybill not found")
        String message
) {
}
