package com.lanely.api.entity;

import com.lanely.api.entity.enums.SubscriptionPlanCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "subscription_plans")
public class SubscriptionPlan extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 32)
    private SubscriptionPlanCode code;

    @Column(nullable = false)
    private int monthlyPriceCents;

    @Column(nullable = false, length = 3)
    private String currency = "EUR";

    @Column(nullable = false)
    private boolean taxIncluded = false;

    @Column(nullable = false)
    private int maxCompanies;

    @Column(nullable = false)
    private int maxSeatsPerCompany;

    @Column(nullable = false)
    private int sortOrder;

    @Column
    private String stripePriceId;

    public SubscriptionPlanCode getCode() {
        return code;
    }

    public void setCode(SubscriptionPlanCode code) {
        this.code = code;
    }

    public int getMonthlyPriceCents() {
        return monthlyPriceCents;
    }

    public void setMonthlyPriceCents(int monthlyPriceCents) {
        this.monthlyPriceCents = monthlyPriceCents;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public boolean isTaxIncluded() {
        return taxIncluded;
    }

    public void setTaxIncluded(boolean taxIncluded) {
        this.taxIncluded = taxIncluded;
    }

    public int getMaxCompanies() {
        return maxCompanies;
    }

    public void setMaxCompanies(int maxCompanies) {
        this.maxCompanies = maxCompanies;
    }

    public int getMaxSeatsPerCompany() {
        return maxSeatsPerCompany;
    }

    public void setMaxSeatsPerCompany(int maxSeatsPerCompany) {
        this.maxSeatsPerCompany = maxSeatsPerCompany;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getStripePriceId() {
        return stripePriceId;
    }

    public void setStripePriceId(String stripePriceId) {
        this.stripePriceId = stripePriceId;
    }
}
