package com.lanely.api.service.payment;

import com.lanely.api.entity.SubscriptionPlan;
import com.lanely.api.entity.User;

public interface SubscriptionGateway {

    SubscriptionChangeOutcome requestPlanChange(User user, SubscriptionPlan targetPlan);
}
