package com.lanely.api.service.payment;

import com.lanely.api.entity.SubscriptionPlan;
import com.lanely.api.entity.User;
import org.springframework.stereotype.Component;

@Component
public class ManualSubscriptionGateway implements SubscriptionGateway {

    @Override
    public SubscriptionChangeOutcome requestPlanChange(User user, SubscriptionPlan targetPlan) {
        return SubscriptionChangeOutcome.activatedNow();
    }
}
