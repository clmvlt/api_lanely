package com.lanely.api.mapper;

import com.lanely.api.dto.pricing.TariffResponse;
import com.lanely.api.dto.pricing.TariffSummaryResponse;
import com.lanely.api.entity.Client;
import com.lanely.api.entity.Tariff;

import java.util.UUID;

public final class TariffMapper {

    private TariffMapper() {
    }

    public static TariffSummaryResponse toSummary(Tariff tariff) {
        return new TariffSummaryResponse(tariff.getId(), tariff.getName(), tariff.getCurrency(), tariff.isDefault(),
                tariff.getStatus(), clientId(tariff.getClient()), tariff.getValidFrom(), tariff.getValidUntil(),
                tariff.getCreatedAt());
    }

    public static TariffResponse toResponse(Tariff tariff) {
        return new TariffResponse(tariff.getId(), tariff.getName(), tariff.getDescription(), tariff.getCurrency(),
                tariff.isDefault(), tariff.getStatus(), clientId(tariff.getClient()), tariff.getValidFrom(),
                tariff.getValidUntil(), tariff.getRoundingMode(), tariff.getRoundingScale(),
                tariff.getMinChargeAmount(),
                tariff.getComponents().stream().map(TariffComponentMapper::toDto).toList(),
                FuelSurchargePolicyMapper.toResponse(tariff.getFuelSurcharge()),
                tariff.getCreatedAt(), tariff.getUpdatedAt());
    }

    private static UUID clientId(Client client) {
        return client == null ? null : client.getId();
    }
}
