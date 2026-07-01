package com.lanely.api.repository;

import com.lanely.api.entity.FuelSurchargePolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FuelSurchargePolicyRepository extends JpaRepository<FuelSurchargePolicy, UUID> {

    Optional<FuelSurchargePolicy> findByTariffId(UUID tariffId);
}
