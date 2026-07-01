package com.lanely.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security.refresh")
public record RefreshProperties(

        long ttlDays
) {
}
