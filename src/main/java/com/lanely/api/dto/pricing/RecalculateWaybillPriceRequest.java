package com.lanely.api.dto.pricing;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(name = "RecalculateWaybillPriceRequest", description = "Optional overrides when recomputing and storing a waybill's carriage charges")
public record RecalculateWaybillPriceRequest(

        @Schema(description = "Force a specific rate card. When null, the grid is resolved from the waybill's client then the company default.",
                example = "9f1c2d3e-4a5b-6c7d-8e9f-0a1b2c3d4e5f", nullable = true)
        UUID tariffId
) {
}
