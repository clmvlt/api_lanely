package com.lanely.api.repository;

import com.lanely.api.entity.VehicleMileageReading;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface VehicleMileageReadingRepository extends JpaRepository<VehicleMileageReading, UUID> {

    Page<VehicleMileageReading> findByVehicleIdOrderByRecordedAtDesc(UUID vehicleId, Pageable pageable);

    Optional<VehicleMileageReading> findTopByVehicleIdOrderByRecordedAtDesc(UUID vehicleId);
}
