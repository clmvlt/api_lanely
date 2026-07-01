package com.lanely.api.dto.subscription;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "MySubscriptionResponse", description = "The current web user's subscription together with their current usage")
public record MySubscriptionResponse(

        @Schema(description = "The plan the user is currently subscribed to, or null if they have no active subscription", nullable = true)
        SubscriptionPlanResponse plan,

        @Schema(description = "Number of companies the user currently owns", example = "1")
        long companiesUsed,

        @Schema(description = "Maximum number of companies allowed by the current plan (0 if no plan)", example = "1")
        int companiesLimit,

        @Schema(description = "Per-company seat usage for every company owned by the user")
        List<CompanySeatUsage> companies
) {
}
