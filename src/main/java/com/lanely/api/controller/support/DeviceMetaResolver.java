package com.lanely.api.controller.support;

import com.lanely.api.service.DeviceMeta;
import jakarta.servlet.http.HttpServletRequest;

public final class DeviceMetaResolver {

    private DeviceMetaResolver() {
    }

    public static DeviceMeta from(HttpServletRequest request) {
        String deviceLabel = request.getHeader("X-Device-Label");
        String userAgent = request.getHeader("User-Agent");
        return new DeviceMeta(deviceLabel, userAgent, clientIp(request));
    }

    private static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
