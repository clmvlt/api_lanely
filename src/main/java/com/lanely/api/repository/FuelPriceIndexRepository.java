package com.lanely.api.repository;

import com.lanely.api.entity.FuelPriceIndex;
import com.lanely.api.entity.enums.FuelType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface FuelPriceIndexRepository extends JpaRepository<FuelPriceIndex, UUID> {

    Optional<FuelPriceIndex> findFirstByFuelTypeOrderByReferenceDateDesc(FuelType fuelType);

    Optional<FuelPriceIndex> findFirstByFuelTypeAndSourceOrderByReferenceDateDesc(FuelType fuelType, String source);

    Optional<FuelPriceIndex> findByFuelTypeAndReferenceDateAndSource(FuelType fuelType, LocalDate referenceDate,
                                                                     String source);

    @Query("select f from FuelPriceIndex f where f.fuelType = :fuelType "
            + "and (:source is null or f.source = :source) "
            + "and (:from is null or f.referenceDate >= :from) "
            + "and (:to is null or f.referenceDate <= :to)")
    Page<FuelPriceIndex> history(@Param("fuelType") FuelType fuelType, @Param("source") String source,
                                 @Param("from") LocalDate from, @Param("to") LocalDate to, Pageable pageable);
}
