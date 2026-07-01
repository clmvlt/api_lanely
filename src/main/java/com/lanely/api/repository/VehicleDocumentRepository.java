package com.lanely.api.repository;

import com.lanely.api.entity.VehicleDocument;
import com.lanely.api.entity.enums.VehicleDocumentCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VehicleDocumentRepository extends JpaRepository<VehicleDocument, UUID> {

    List<VehicleDocument> findByVehicleIdOrderByCreatedAtDesc(UUID vehicleId);

    List<VehicleDocument> findByVehicleIdAndCategoryOrderByCreatedAtDesc(UUID vehicleId, VehicleDocumentCategory category);

    Optional<VehicleDocument> findByIdAndVehicleId(UUID id, UUID vehicleId);
}
