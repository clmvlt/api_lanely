package com.lanely.api.mapper;

import com.lanely.api.dto.pricing.TariffComponentDto;
import com.lanely.api.entity.TariffComponent;

public final class TariffComponentMapper {

    private TariffComponentMapper() {
    }

    public static TariffComponentDto toDto(TariffComponent component) {
        return new TariffComponentDto(component.getId(), component.getPosition(), component.getLabel(),
                component.getBasis(), component.getKind(), component.getUnitPrice(), component.getIncludedQuantity(),
                component.getMinQuantity(), component.getMaxQuantity(), component.getMinAmount(),
                component.getMaxAmount());
    }
}
