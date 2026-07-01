package com.lanely.api.mapper;

import com.lanely.api.dto.common.StatusActorDto;
import com.lanely.api.dto.common.StatusHistoryResponse;
import com.lanely.api.entity.AbstractStatusHistory;
import com.lanely.api.entity.ParcelStatusHistory;
import com.lanely.api.entity.TourStatusHistory;
import com.lanely.api.entity.WaybillStatusHistory;

public final class StatusHistoryMapper {

    private StatusHistoryMapper() {
    }

    public static StatusHistoryResponse toResponse(WaybillStatusHistory entry) {
        return new StatusHistoryResponse(entry.getId(),
                entry.getFromStatus() == null ? null : entry.getFromStatus().name(),
                entry.getToStatus().name(), actor(entry), entry.getNote(),
                entry.getLatitude(), entry.getLongitude(), entry.getChangedAt());
    }

    public static StatusHistoryResponse toResponse(TourStatusHistory entry) {
        return new StatusHistoryResponse(entry.getId(),
                entry.getFromStatus() == null ? null : entry.getFromStatus().name(),
                entry.getToStatus().name(), actor(entry), entry.getNote(),
                entry.getLatitude(), entry.getLongitude(), entry.getChangedAt());
    }

    public static StatusHistoryResponse toResponse(ParcelStatusHistory entry) {
        return new StatusHistoryResponse(entry.getId(),
                entry.getFromStatus() == null ? null : entry.getFromStatus().name(),
                entry.getToStatus().name(), actor(entry), entry.getNote(),
                entry.getLatitude(), entry.getLongitude(), entry.getChangedAt());
    }

    private static StatusActorDto actor(AbstractStatusHistory entry) {
        return new StatusActorDto(entry.getChangedBy() == null ? null : entry.getChangedBy().getId(),
                entry.getActorType(), entry.getChangedByName());
    }
}
