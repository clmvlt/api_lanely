package com.lanely.api.dto.waybill;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "BulkResultResponse", description = "Result of a bulk operation. Returned with HTTP 200 even when some items failed; "
        + "inspect succeeded/failed and the per-item results. The request itself returns 400 only when it is invalid (empty batch, "
        + "too many ids, malformed body).")
public record BulkResultResponse(

        @Schema(description = "Number of items processed successfully", example = "8")
        int succeeded,

        @Schema(description = "Number of items that failed", example = "2")
        int failed,

        @Schema(description = "Per-item outcomes, in the order the ids were supplied (duplicates are processed once each)")
        List<BulkItemResult> results
) {

    public static BulkResultResponse of(List<BulkItemResult> results) {
        int succeeded = (int) results.stream().filter(r -> r.status() == BulkItemStatus.OK).count();
        return new BulkResultResponse(succeeded, results.size() - succeeded, results);
    }
}
