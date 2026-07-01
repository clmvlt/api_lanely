package com.lanely.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ApiInfo", description = "Public, non-sensitive runtime information about the API")
public record ApiInfoDto(

        @Schema(description = "Application name", example = "api-lanely")
        String application,

        @Schema(description = "Current API version", example = "1.0.0")
        String version,

        @Schema(description = "Active environment (Spring profile)", example = "prod")
        String environment,

        @Schema(description = "Current health status of the API", example = "UP")
        String status,

        @Schema(description = "Instant the API started, ISO-8601 (UTC)", example = "2026-06-10T09:15:30Z")
        String startedAt,

        @Schema(description = "Human-readable uptime since startup", example = "1h 23m 45s")
        String uptime,

        @Schema(description = "Uptime since startup, in milliseconds", example = "5025000")
        long uptimeMillis

) {
}
