package com.lanely.api.repository;

import com.lanely.api.entity.Vehicle;
import com.lanely.api.entity.enums.VehicleStatus;
import com.lanely.api.entity.enums.VehicleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {

    Optional<Vehicle> findByIdAndCompanyId(UUID id, UUID companyId);

    boolean existsByCompanyIdAndRegistrationPlate(UUID companyId, String registrationPlate);

    @Query("select v from Vehicle v where v.company.id = :companyId "
            + "and (:status is null or v.status = :status) "
            + "and (:type is null or v.vehicleType = :type) "
            + "and (lower(v.registrationPlate) like :pattern "
            + "  or lower(coalesce(v.make, '')) like :pattern "
            + "  or lower(coalesce(v.model, '')) like :pattern "
            + "  or lower(coalesce(v.vin, '')) like :pattern)")
    Page<Vehicle> search(@Param("companyId") UUID companyId, @Param("status") VehicleStatus status,
                         @Param("type") VehicleType type, @Param("pattern") String pattern, Pageable pageable);
}
