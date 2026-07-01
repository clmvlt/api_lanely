package com.lanely.api.dto.subscription;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ChangeSubscriptionResponse", description = "Result of a plan-change request")
public record ChangeSubscriptionResponse(

        @Schema(description = "Whether the new plan is already active or still waiting for an external payment step", example = "ACTIVATED")
        SubscriptionChangeStatus status,

        @Schema(description = "The user's subscription and usage after the request. Reflects the new plan when status is ACTIVATED, "
                + "or the unchanged current plan when status is PENDING_PAYMENT.")
        MySubscriptionResponse subscription,

        @Schema(description = "URL the client must redirect to in order to complete payment. Always null today; populated once an external "
                + "payment provider (e.g. Stripe) is wired in and status is PENDING_PAYMENT.", nullable = true)
        String checkoutUrl
) {
}
