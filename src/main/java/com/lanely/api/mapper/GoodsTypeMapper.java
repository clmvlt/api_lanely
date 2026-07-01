package com.lanely.api.mapper;

import com.lanely.api.dto.goodstype.GoodsTypeResponse;
import com.lanely.api.entity.GoodsType;

public final class GoodsTypeMapper {

    private GoodsTypeMapper() {
    }

    public static GoodsTypeResponse toResponse(GoodsType goodsType) {
        return new GoodsTypeResponse(goodsType.getId(), goodsType.getName(), goodsType.getDescription(),
                goodsType.getPackagingType(), goodsType.getNumberOfPackages(), goodsType.getGrossWeightKg(),
                goodsType.getVolumeM3(), goodsType.getLengthCm(), goodsType.getWidthCm(), goodsType.getHeightCm(),
                goodsType.isDangerousGoods(), goodsType.getUnNumber(),
                goodsType.getCreatedAt(), goodsType.getUpdatedAt());
    }
}
