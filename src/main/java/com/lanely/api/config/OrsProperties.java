package com.lanely.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.ors")
public record OrsProperties(

        String baseUrl,
        Duration connectTimeout,
        Duration readTimeout
) {

    public Duration connectTimeoutOrDefault() {
        return connectTimeout == null ? Duration.ofSeconds(5) : connectTimeout;
    }

    public Duration readTimeoutOrDefault() {
        return readTimeout == null ? Duration.ofSeconds(30) : readTimeout;
    }
}
