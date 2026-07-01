package com.lanely.api.dto.goodstype;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(name = "CreateGoodsTypeRequest",
        description = "Payload to create a reusable goods type in the company's catalog. Goods types are decoupled from waybills: "
                + "they only feed autocompletion and may carry default values pre-filled when a type is picked while editing a waybill goods line.")
public record CreateGoodsTypeRequest(

        @Schema(description = "Display name / autocompletion label, unique within the company (case-insensitive)",
                example = "Pallet of auto parts", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        @Size(max = 120)
        String name,

        @Schema(description = "Free-form description of the goods type", example = "Standard EUR pallet of palletized auto parts", nullable = true)
        String description,

        @Schema(description = "Default method of packing", example = "Pallet", nullable = true)
        @Size(max = 64)
        String packagingType,

        @Schema(description = "Default number of packages", example = "8", nullable = true)
        @PositiveOrZero
        Integer numberOfPackages,

        @Schema(description = "Default gross weight in kilograms", example = "1250.500", nullable = true)
        @PositiveOrZero
        BigDecimal grossWeightKg,

        @Schema(description = "Default volume in cubic meters", example = "3.200", nullable = true)
        @PositiveOrZero
        BigDecimal volumeM3,

        @Schema(description = "Default length in centimeters", example = "120.00", nullable = true)
        @PositiveOrZero
        BigDecimal lengthCm,

        @Schema(description = "Default width in centimeters", example = "80.00", nullable = true)
        @PositiveOrZero
        BigDecimal widthCm,

        @Schema(description = "Default height in centimeters", example = "100.00", nullable = true)
        @PositiveOrZero
        BigDecimal heightCm,

        @Schema(description = "Whether goods of this type are dangerous by default (ADR)", example = "false", defaultValue = "false", nullable = true)
        Boolean dangerousGoods,

        @Schema(description = "Default UN number when dangerous goods", example = "1203", nullable = true)
        @Size(max = 8)
        String unNumber
) {
}
