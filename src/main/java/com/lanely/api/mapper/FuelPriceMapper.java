package com.lanely.api.mapper;

import com.lanely.api.dto.fuel.FuelPriceResponse;
import com.lanely.api.entity.FuelPriceIndex;

public final class FuelPriceMapper {

    private FuelPriceMapper() {
    }

    public static FuelPriceResponse toResponse(FuelPriceIndex index) {
        return new FuelPriceResponse(index.getId(), index.getFuelType(), index.getPrice(), index.getCurrency(),
                index.getReferenceDate(), index.getSource(), index.getFetchedAt());
    }
}
