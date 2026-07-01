package com.lanely.api.dto.client;

import com.lanely.api.dto.company.AddressDto;
import com.lanely.api.entity.enums.AddressType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(name = "ClientAddressResponse", description = "An address of a client (depot, billing, shipping, headquarters...)")
public record ClientAddressResponse(

        @Schema(description = "Unique address identifier", example = "1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d")
        UUID id,

        @Schema(description = "Human-friendly label", example = "North depot", nullable = true)
        String label,

        @Schema(description = "Address category", example = "DEPOT")
        AddressType type,

        @Schema(description = "Postal address")
        AddressDto address,

        @Schema(description = "Latitude (WGS84, decimal degrees), for route distance computation", example = "48.8566", nullable = true)
        Double latitude,

        @Schema(description = "Longitude (WGS84, decimal degrees), for route distance computation", example = "2.3522", nullable = true)
        Double longitude,

        @Schema(description = "Whether this is the client's primary address", example = "true")
        boolean isPrimary,

        @Schema(description = "Whether this is the default address for billing", example = "false")
        boolean isDefaultBilling,

        @Schema(description = "Whether this is the default address for shipping/delivery", example = "true")
        boolean isDefaultShipping,

        @Schema(description = "On-site contact name", example = "Marie Dupont", nullable = true)
        String contactName,

        @Schema(description = "On-site contact phone", example = "+33123456789", nullable = true)
        String contactPhone,

        @Schema(description = "On-site contact e-mail", example = "depot.north@acme.example", nullable = true)
        String contactEmail,

        @Schema(description = "Access notes / delivery instructions for drivers", example = "Ring at gate B, trucks under 12m only.", nullable = true)
        String deliveryInstructions,

        @Schema(description = "Whether this address is active and selectable", example = "true")
        boolean active,

        @Schema(type = "string", format = "date-time", description = "Creation instant (ISO-8601 UTC)", example = "2026-06-10T09:15:30Z")
        Instant createdAt,

        @Schema(type = "string", format = "date-time", description = "Last update instant (ISO-8601 UTC)", example = "2026-06-10T09:15:30Z")
        Instant updatedAt
) {
}
