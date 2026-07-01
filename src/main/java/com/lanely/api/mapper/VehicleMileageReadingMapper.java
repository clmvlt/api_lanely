package com.lanely.api.mapper;

import com.lanely.api.dto.vehicle.MileageReadingResponse;
import com.lanely.api.entity.Account;
import com.lanely.api.entity.VehicleMileageReading;

public final class VehicleMileageReadingMapper {

    private VehicleMileageReadingMapper() {
    }

    public static MileageReadingResponse toResponse(VehicleMileageReading reading) {
        Account recordedBy = reading.getRecordedBy();
        return new MileageReadingResponse(reading.getId(), reading.getValueKm(), reading.getRecordedAt(),
                recordedBy == null ? null : recordedBy.getId(), reading.getNote(), reading.getCreatedAt());
    }
}
