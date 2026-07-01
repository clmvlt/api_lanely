package com.lanely.api.dto.waybill;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(name = "DockSummaryResponse", description = "Aggregated totals of the goods currently at the dock for a company")
public record DockSummaryResponse(

        @Schema(description = "Number of waybills currently at the dock (status AT_DOCK or holding at least one goods line AT_DOCK)",
                example = "12")
        long waybillCount,

        @Schema(description = "Total number of packages currently at the dock", example = "340")
        long totalPackages,

        @Schema(description = "Total gross weight currently at the dock, in kilograms", example = "12540.500")
        BigDecimal totalGrossWeightKg,

        @Schema(description = "Total volume currently at the dock, in cubic meters", example = "85.200")
        BigDecimal totalVolumeM3
) {
}
