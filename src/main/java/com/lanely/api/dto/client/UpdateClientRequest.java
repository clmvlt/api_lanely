package com.lanely.api.dto.client;

import com.lanely.api.dto.company.LegalInfoDto;
import com.lanely.api.entity.enums.ClientType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Schema(name = "UpdateClientRequest", description = "Payload to update a client. Only non-null fields are applied.")
public record UpdateClientRequest(

        @Schema(description = "New reference, unique within the company. Null to keep unchanged.", example = "CLI-0042", nullable = true)
        @Size(min = 1, max = 32)
        String reference,

        @Schema(description = "New client type. Null to keep unchanged.", example = "INDIVIDUAL", nullable = true)
        ClientType type,

        @Schema(description = "New display name. Null to keep unchanged.", example = "ACME Logistics SAS", nullable = true)
        @Size(min = 1, max = 200)
        String name,

        @Schema(description = "Legal identification (replaces the whole block when provided). Null to keep unchanged.", nullable = true)
        @Valid
        LegalInfoDto legalInfo,

        @Schema(description = "New main e-mail. Null to keep unchanged.", example = "contact@acme.example", nullable = true)
        @Email
        @Size(max = 255)
        String email,

        @Schema(description = "New main phone. Null to keep unchanged.", example = "+33123456789", nullable = true)
        @Size(max = 32)
        String phone,

        @Schema(description = "New website. Null to keep unchanged.", example = "https://acme.example", nullable = true)
        @Size(max = 255)
        String website,

        @Schema(description = "New default payment term in days. Null to keep unchanged.", example = "45", nullable = true, minimum = "0", maximum = "365")
        @Min(0)
        @Max(365)
        Integer paymentTermsDays,

        @Schema(description = "Whether deliveries for this client are blocked (e.g. unpaid invoices). Null to keep unchanged.", example = "false", nullable = true)
        Boolean deliveryBlocked,

        @Schema(description = "Identifier of the company member (its user id) managing this client. Null to keep unchanged.", example = "8a1f2c3b-4d5e-6f70-3f1c-8d2e9b4a4d6e", nullable = true)
        UUID accountManagerUserId,

        @Schema(description = "New internal notes. Null to keep unchanged.", example = "Switched to weekly billing.", nullable = true)
        String notes,

        @Schema(description = "Per-client preferences. Only non-null inner fields are applied. Null to keep unchanged.", nullable = true)
        @Valid
        ClientSettingsDto settings
) {
}
