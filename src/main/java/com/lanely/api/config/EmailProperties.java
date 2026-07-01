package com.lanely.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.email")
public record EmailProperties(

        String provider,

        String from,

        String supportUrl
) {
}
