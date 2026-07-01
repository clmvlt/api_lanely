package com.lanely.api.dto.waybill;

import com.lanely.api.entity.enums.ParcelStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Schema(name = "GoodsLineResponse", description = "A line of goods (parcel) carried under the waybill")
public record GoodsLineResponse(

        @Schema(description = "Goods line identifier", example = "3a4b5c6d-7e8f-9a0b-1c2d-3e4f5a6b7c8d")
        UUID id,

        @Schema(description = "Zero-based position in the goods list", example = "0")
        int position,

        @Schema(description = "Current parcel status", example = "PENDING")
        ParcelStatus status,

        @Schema(description = "Description / nature of the goods", example = "Palletized auto parts")
        String description,

        @Schema(description = "Method of packing", example = "Pallet", nullable = true)
        String packagingType,

        @Schema(description = "Number of packages", example = "8", nullable = true)
        Integer numberOfPackages,

        @Schema(description = "Marks and numbers", example = "PAL-001..008", nullable = true)
        String marksAndNumbers,

        @Schema(description = "Gross weight in kilograms", example = "1250.500", nullable = true)
        BigDecimal grossWeightKg,

        @Schema(description = "Volume in cubic meters", example = "3.200", nullable = true)
        BigDecimal volumeM3,

        @Schema(description = "Length in centimeters", example = "120.00", nullable = true)
        BigDecimal lengthCm,

        @Schema(description = "Width in centimeters", example = "80.00", nullable = true)
        BigDecimal widthCm,

        @Schema(description = "Height in centimeters", example = "100.00", nullable = true)
        BigDecimal heightCm,

        @Schema(description = "Whether the goods are dangerous (ADR)", example = "false")
        boolean dangerousGoods,

        @Schema(description = "UN number when dangerous goods", example = "1203", nullable = true)
        String unNumber,

        @Schema(type = "string", format = "date-time",
                description = "Instant this goods line last entered the dock (ISO-8601 UTC), null when it never went to the dock",
                example = "2026-06-10T09:15:30Z", nullable = true)
        Instant dockEnteredAt,

        @Schema(type = "string", format = "date-time",
                description = "Instant this goods line last left the dock (ISO-8601 UTC), null while still at dock or never docked",
                example = "2026-06-17T07:30:00Z", nullable = true)
        Instant dockExitedAt
) {
}
