package com.lanely.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.invitation")
public record InvitationProperties(

        long ttlDays
) {
}
