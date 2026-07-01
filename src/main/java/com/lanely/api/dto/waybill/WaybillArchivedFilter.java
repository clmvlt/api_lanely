package com.lanely.api.dto.waybill;

import com.lanely.api.exception.BadRequestException;

import java.util.Locale;

public enum WaybillArchivedFilter {

    ACTIVE,
    ARCHIVED,
    ALL;

    public static WaybillArchivedFilter parse(String raw) {
        if (raw == null || raw.isBlank() || raw.equalsIgnoreCase("false")) {
            return ACTIVE;
        }
        String normalized = raw.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "true" -> ARCHIVED;
            case "all" -> ALL;
            default -> throw new BadRequestException("error.request.param-invalid", "archived");
        };
    }

    public Boolean toArchivedFlag() {
        return switch (this) {
            case ACTIVE -> Boolean.FALSE;
            case ARCHIVED -> Boolean.TRUE;
            case ALL -> null;
        };
    }
}
