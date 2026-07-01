package com.lanely.api.dto.session;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(name = "SessionResponse", description = "An active or past login session for the authenticated account")
public record SessionResponse(

        @Schema(description = "Unique session identifier", example = "aaaa1111-bbbb-2222-cccc-3333dddd4444")
        UUID id,

        @Schema(description = "Optional human-friendly device label", example = "iPhone 15 - Mobile App", nullable = true)
        String deviceLabel,

        @Schema(description = "User-Agent captured at login", example = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)", nullable = true)
        String userAgent,

        @Schema(description = "IP address captured at login", example = "203.0.113.42", nullable = true)
        String ipAddress,

        @Schema(description = "Instant the session was created (ISO-8601 UTC)", example = "2026-06-10T09:00:00Z")
        Instant createdAt,

        @Schema(description = "Instant the session was last refreshed (ISO-8601 UTC)", example = "2026-06-10T11:30:00Z")
        Instant lastUsedAt,

        @Schema(description = "Instant the refresh token expires (ISO-8601 UTC)", example = "2026-07-10T09:00:00Z")
        Instant expiresAt,

        @Schema(description = "True when this is the session used for the current request", example = "true")
        boolean current
) {
}
