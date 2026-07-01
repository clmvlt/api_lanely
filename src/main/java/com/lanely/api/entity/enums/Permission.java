package com.lanely.api.entity.enums;

public enum Permission {

    MANAGE_COMPANY("Edit company information and invite members (acts like an owner for company management)"),
    MANAGE_PROFILES("Create, update, delete, activate and deactivate delivery profiles"),
    MANAGE_PERMISSIONS("Grant and revoke permissions to company members"),
    MANAGE_CLIENTS("Create, update, archive clients and manage their addresses and contacts"),
    MANAGE_VEHICLES("Create, update, delete vehicles and manage their documents and photos"),
    MANAGE_TRANSPORTS("Create, update and manage waybills (consignment notes) and tours, including their assignment and lifecycle"),
    MANAGE_PRICING("Create, update and delete pricing rate cards (tariffs), their components and fuel surcharge policies, and trigger fuel price refreshes");

    private final String description;

    Permission(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
