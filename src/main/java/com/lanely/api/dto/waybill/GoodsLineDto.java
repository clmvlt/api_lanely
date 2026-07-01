package com.lanely.api.dto.waybill;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(name = "GoodsLineDto", description = "A line of goods carried under the waybill (CMR boxes 6-12)")
public record GoodsLineDto(

        @Schema(description = "Description / nature of the goods", example = "Palletized auto parts", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        @Size(max = 500)
        String description,

        @Schema(description = "Method of packing", example = "Pallet", nullable = true)
        @Size(max = 64)
        String packagingType,

        @Schema(description = "Number of packages", example = "8", nullable = true)
        @PositiveOrZero
        Integer numberOfPackages,

        @Schema(description = "Marks and numbers identifying the packages", example = "PAL-001..008", nullable = true)
        @Size(max = 255)
        String marksAndNumbers,

        @Schema(description = "Gross weight in kilograms", example = "1250.500", nullable = true)
        @PositiveOrZero
        BigDecimal grossWeightKg,

        @Schema(description = "Volume in cubic meters", example = "3.200", nullable = true)
        @PositiveOrZero
        BigDecimal volumeM3,

        @Schema(description = "Length in centimeters", example = "120.00", nullable = true)
        @PositiveOrZero
        BigDecimal lengthCm,

        @Schema(description = "Width in centimeters", example = "80.00", nullable = true)
        @PositiveOrZero
        BigDecimal widthCm,

        @Schema(description = "Height in centimeters", example = "100.00", nullable = true)
        @PositiveOrZero
        BigDecimal heightCm,

        @Schema(description = "Whether the goods are dangerous (ADR)", example = "false", defaultValue = "false", nullable = true)
        Boolean dangerousGoods,

        @Schema(description = "UN number when dangerous goods", example = "1203", nullable = true)
        @Size(max = 8)
        String unNumber
) {
}
