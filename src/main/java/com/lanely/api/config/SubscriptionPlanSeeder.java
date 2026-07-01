package com.lanely.api.config;

import com.lanely.api.entity.SubscriptionPlan;
import com.lanely.api.entity.enums.SubscriptionPlanCode;
import com.lanely.api.repository.SubscriptionPlanRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class SubscriptionPlanSeeder implements ApplicationRunner {

    private record PlanSeed(SubscriptionPlanCode code, int monthlyPriceCents, int maxCompanies, int maxSeatsPerCompany,
                            int sortOrder) {
    }

    private static final List<PlanSeed> SEEDS = List.of(
            new PlanSeed(SubscriptionPlanCode.STARTER, 3000, 1, 10, 1),
            new PlanSeed(SubscriptionPlanCode.PRO, 5000, 1, 50, 2),
            new PlanSeed(SubscriptionPlanCode.ENTERPRISE, 10000, 10, 200, 3)
    );

    private final SubscriptionPlanRepository planRepository;

    public SubscriptionPlanSeeder(SubscriptionPlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (PlanSeed seed : SEEDS) {
            SubscriptionPlan plan = planRepository.findByCode(seed.code()).orElseGet(SubscriptionPlan::new);
            plan.setCode(seed.code());
            plan.setMonthlyPriceCents(seed.monthlyPriceCents());
            plan.setCurrency("EUR");
            plan.setTaxIncluded(false);
            plan.setMaxCompanies(seed.maxCompanies());
            plan.setMaxSeatsPerCompany(seed.maxSeatsPerCompany());
            plan.setSortOrder(seed.sortOrder());
            planRepository.save(plan);
        }
    }
}
