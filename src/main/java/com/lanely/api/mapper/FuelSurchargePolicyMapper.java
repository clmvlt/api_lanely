package com.lanely.api.mapper;

import com.lanely.api.dto.pricing.FuelSurchargeComponentDto;
import com.lanely.api.dto.pricing.FuelSurchargePolicyResponse;
import com.lanely.api.entity.FuelSurchargeComponent;
import com.lanely.api.entity.FuelSurchargePolicy;

public final class FuelSurchargePolicyMapper {

    private FuelSurchargePolicyMapper() {
    }

    public static FuelSurchargePolicyResponse toResponse(FuelSurchargePolicy policy) {
        if (policy == null) {
            return null;
        }
        return new FuelSurchargePolicyResponse(policy.getId(), policy.isEnabled(), policy.getFuelType(),
                policy.getMode(), policy.getThresholdPrice(), policy.getReferencePrice(), policy.getDieselShareRatio(),
                policy.isClampAtZero(), policy.getSourceFilter(),
                policy.getSurchargeComponents().stream().map(FuelSurchargePolicyMapper::toComponentDto).toList());
    }

    public static FuelSurchargeComponentDto toComponentDto(FuelSurchargeComponent component) {
        return new FuelSurchargeComponentDto(component.getId(), component.getPosition(), component.getLabel(),
                component.getBasis(), component.getUnitPrice(), component.getIncludedQuantity(),
                component.getMinQuantity(), component.getMaxQuantity(), component.getMinAmount(),
                component.getMaxAmount());
    }
}
