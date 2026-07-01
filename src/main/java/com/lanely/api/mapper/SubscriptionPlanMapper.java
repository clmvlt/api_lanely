package com.lanely.api.mapper;

import com.lanely.api.dto.subscription.SubscriptionPlanResponse;
import com.lanely.api.entity.SubscriptionPlan;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class SubscriptionPlanMapper {

    private final MessageSource messageSource;

    public SubscriptionPlanMapper(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public SubscriptionPlanResponse toResponse(SubscriptionPlan plan) {
        String code = plan.getCode().name();
        String name = resolve("subscription.plan." + code.toLowerCase() + ".name");
        String description = resolve("subscription.plan." + code.toLowerCase() + ".description");
        BigDecimal amount = BigDecimal.valueOf(plan.getMonthlyPriceCents()).movePointLeft(2);
        return new SubscriptionPlanResponse(code, name, description, plan.getMonthlyPriceCents(), amount,
                plan.getCurrency(), plan.isTaxIncluded(), plan.getMaxCompanies(), plan.getMaxSeatsPerCompany(),
                plan.getSortOrder());
    }

    private String resolve(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }
}
