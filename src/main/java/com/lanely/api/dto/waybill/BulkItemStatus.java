package com.lanely.api.dto.waybill;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "BulkItemStatus", description = "Outcome of a single item in a bulk operation")
public enum BulkItemStatus {
    OK,
    ERROR
}
