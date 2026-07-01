package com.lanely.api.dto.client;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(name = "ClientContactResponse", description = "A contact person attached to a client")
public record ClientContactResponse(

        @Schema(description = "Unique contact identifier", example = "2b3c4d5e-6f7a-8b9c-0d1e-2f3a4b5c6d7e")
        UUID id,

        @Schema(description = "First name", example = "Marie", nullable = true)
        String firstName,

        @Schema(description = "Last name", example = "Dupont", nullable = true)
        String lastName,

        @Schema(description = "Job title / role", example = "Logistics manager", nullable = true)
        String jobTitle,

        @Schema(description = "E-mail address", example = "marie.dupont@acme.example", nullable = true)
        String email,

        @Schema(description = "Phone number", example = "+33123456789", nullable = true)
        String phone,

        @Schema(description = "Whether this is the client's primary contact", example = "true")
        boolean isPrimary,

        @Schema(description = "Whether this contact receives invoices by e-mail", example = "true")
        boolean receivesInvoices,

        @Schema(description = "Whether this contact receives delivery notifications by e-mail", example = "false")
        boolean receivesDeliveryNotifications,

        @Schema(description = "Whether this contact is active", example = "true")
        boolean active,

        @Schema(type = "string", format = "date-time", description = "Creation instant (ISO-8601 UTC)", example = "2026-06-10T09:15:30Z")
        Instant createdAt,

        @Schema(type = "string", format = "date-time", description = "Last update instant (ISO-8601 UTC)", example = "2026-06-10T09:15:30Z")
        Instant updatedAt
) {
}
