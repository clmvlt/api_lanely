package com.lanely.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app.security.google")
public record GoogleProperties(

        List<String> clientIds
) {
}
