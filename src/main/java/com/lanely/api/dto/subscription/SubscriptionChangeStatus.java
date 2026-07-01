package com.lanely.api.dto.subscription;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "SubscriptionChangeStatus", description = "Outcome of a plan-change request. ACTIVATED means the plan is effective "
        + "immediately. PENDING_PAYMENT means the change waits for an external payment step (e.g. a Stripe checkout) before it takes effect.")
public enum SubscriptionChangeStatus {

    ACTIVATED,
    PENDING_PAYMENT
}
