package com.lanely.api.dto.client;

import com.lanely.api.dto.company.LegalInfoDto;
import com.lanely.api.entity.enums.ClientStatus;
import com.lanely.api.entity.enums.ClientType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(name = "ClientResponse", description = "A client of the transport company, with its addresses and contacts")
public record ClientResponse(

        @Schema(description = "Unique client identifier", example = "9f1c2d3e-4a5b-6c7d-8e9f-0a1b2c3d4e5f")
        UUID id,

        @Schema(description = "Reference, unique within the company", example = "CLI-0001")
        String reference,

        @Schema(description = "Client type", example = "COMPANY")
        ClientType type,

        @Schema(description = "Display name", example = "ACME Logistics")
        String name,

        @Schema(description = "Legal identification", nullable = true)
        LegalInfoDto legalInfo,

        @Schema(description = "Main e-mail address", example = "contact@acme.example", nullable = true)
        String email,

        @Schema(description = "Main phone number", example = "+33123456789", nullable = true)
        String phone,

        @Schema(description = "Website URL", example = "https://acme.example", nullable = true)
        String website,

        @Schema(description = "Default payment term in days", example = "30")
        int paymentTermsDays,

        @Schema(description = "Lifecycle status", example = "ACTIVE")
        ClientStatus status,

        @Schema(description = "Whether deliveries for this client are blocked", example = "false")
        boolean deliveryBlocked,

        @Schema(description = "User id of the company member managing this client", example = "8a1f2c3b-4d5e-6f70-3f1c-8d2e9b4a4d6e", nullable = true)
        UUID accountManagerUserId,

        @Schema(description = "Free-form internal notes", example = "Key account.", nullable = true)
        String notes,

        @Schema(description = "Per-client preferences")
        ClientSettingsDto settings,

        @Schema(description = "Client addresses (depots, billing, shipping...)")
        List<ClientAddressResponse> addresses,

        @Schema(description = "Client contacts (people)")
        List<ClientContactResponse> contacts,

        @Schema(type = "string", format = "date-time", description = "Creation instant (ISO-8601 UTC)", example = "2026-06-10T09:15:30Z")
        Instant createdAt,

        @Schema(type = "string", format = "date-time", description = "Last update instant (ISO-8601 UTC)", example = "2026-06-10T09:15:30Z")
        Instant updatedAt
) {
}
