package com.lanely.api.dto.client;

import com.lanely.api.entity.enums.ClientStatus;
import com.lanely.api.entity.enums.ClientType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(name = "ClientSummaryResponse", description = "A client as listed in a paginated collection (lightweight projection)")
public record ClientSummaryResponse(

        @Schema(description = "Unique client identifier", example = "9f1c2d3e-4a5b-6c7d-8e9f-0a1b2c3d4e5f")
        UUID id,

        @Schema(description = "Reference, unique within the company", example = "CLI-0001")
        String reference,

        @Schema(description = "Client type", example = "COMPANY")
        ClientType type,

        @Schema(description = "Display name", example = "ACME Logistics")
        String name,

        @Schema(description = "Main e-mail address", example = "contact@acme.example", nullable = true)
        String email,

        @Schema(description = "Main phone number", example = "+33123456789", nullable = true)
        String phone,

        @Schema(description = "Lifecycle status", example = "ACTIVE")
        ClientStatus status,

        @Schema(description = "Whether deliveries for this client are blocked", example = "false")
        boolean deliveryBlocked,

        @Schema(type = "string", format = "date-time", description = "Creation instant (ISO-8601 UTC)", example = "2026-06-10T09:15:30Z")
        Instant createdAt
) {
}
