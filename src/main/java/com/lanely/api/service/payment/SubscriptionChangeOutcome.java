package com.lanely.api.service.payment;

public record SubscriptionChangeOutcome(boolean activateImmediately, String checkoutUrl) {

    public static SubscriptionChangeOutcome activatedNow() {
        return new SubscriptionChangeOutcome(true, null);
    }

    public static SubscriptionChangeOutcome pendingCheckout(String checkoutUrl) {
        return new SubscriptionChangeOutcome(false, checkoutUrl);
    }
}
