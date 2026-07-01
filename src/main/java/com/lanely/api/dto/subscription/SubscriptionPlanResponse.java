package com.lanely.api.dto.subscription;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(name = "SubscriptionPlanResponse", description = "A subscription plan offered to web users, with its localized label, price and limits")
public record SubscriptionPlanResponse(

        @Schema(description = "Stable technical code of the plan (never localized, safe to switch on)", example = "STARTER")
        String code,

        @Schema(description = "Localized display name of the plan (resolved from the Accept-Language header)", example = "Starter")
        String name,

        @Schema(description = "Localized marketing description of the plan (resolved from the Accept-Language header)",
                example = "Manage one company with up to 10 profiles and members.")
        String description,

        @Schema(description = "Monthly price in the smallest currency unit (cents). Stripe-ready amount.", example = "3000")
        int monthlyPriceCents,

        @Schema(description = "Monthly price as a decimal amount in the plan currency", example = "30.00")
        BigDecimal monthlyPriceAmount,

        @Schema(description = "ISO-4217 currency code of the price", example = "EUR")
        String currency,

        @Schema(description = "Whether the price already includes tax. False means the amount is tax-excluded (HT).", example = "false")
        boolean taxIncluded,

        @Schema(description = "Maximum number of companies a user on this plan may own", example = "1")
        int maxCompanies,

        @Schema(description = "Maximum number of seats (active profiles + members) allowed per company", example = "10")
        int maxSeatsPerCompany,

        @Schema(description = "Suggested display order, ascending (cheapest/smallest first)", example = "1")
        int sortOrder
) {
}
