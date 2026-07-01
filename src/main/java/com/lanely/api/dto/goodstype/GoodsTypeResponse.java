package com.lanely.api.dto.goodstype;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Schema(name = "GoodsTypeResponse", description = "A reusable goods type in the company's catalog, with its default values for autocompletion")
public record GoodsTypeResponse(

        @Schema(description = "Goods type identifier", example = "3a4b5c6d-7e8f-9a0b-1c2d-3e4f5a6b7c8d")
        UUID id,

        @Schema(description = "Display name / autocompletion label", example = "Pallet of auto parts")
        String name,

        @Schema(description = "Free-form description of the goods type", example = "Standard EUR pallet of palletized auto parts", nullable = true)
        String description,

        @Schema(description = "Default method of packing", example = "Pallet", nullable = true)
        String packagingType,

        @Schema(description = "Default number of packages", example = "8", nullable = true)
        Integer numberOfPackages,

        @Schema(description = "Default gross weight in kilograms", example = "1250.500", nullable = true)
        BigDecimal grossWeightKg,

        @Schema(description = "Default volume in cubic meters", example = "3.200", nullable = true)
        BigDecimal volumeM3,

        @Schema(description = "Default length in centimeters", example = "120.00", nullable = true)
        BigDecimal lengthCm,

        @Schema(description = "Default width in centimeters", example = "80.00", nullable = true)
        BigDecimal widthCm,

        @Schema(description = "Default height in centimeters", example = "100.00", nullable = true)
        BigDecimal heightCm,

        @Schema(description = "Whether goods of this type are dangerous by default (ADR)", example = "false")
        boolean dangerousGoods,

        @Schema(description = "Default UN number when dangerous goods", example = "1203", nullable = true)
        String unNumber,

        @Schema(type = "string", format = "date-time", description = "Creation instant (ISO-8601 UTC)", example = "2026-06-10T09:15:30Z")
        Instant createdAt,

        @Schema(type = "string", format = "date-time", description = "Last update instant (ISO-8601 UTC)", example = "2026-06-12T14:20:00Z")
        Instant updatedAt
) {
}
