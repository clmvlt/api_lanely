package com.lanely.api.repository;

import com.lanely.api.entity.SubscriptionPlan;
import com.lanely.api.entity.enums.SubscriptionPlanCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, UUID> {

    Optional<SubscriptionPlan> findByCode(SubscriptionPlanCode code);

    List<SubscriptionPlan> findAllByOrderBySortOrderAsc();
}
