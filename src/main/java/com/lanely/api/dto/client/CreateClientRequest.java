package com.lanely.api.dto.client;

import com.lanely.api.dto.company.LegalInfoDto;
import com.lanely.api.entity.enums.ClientType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Schema(name = "CreateClientRequest", description = "Payload to create a client of the transport company")
public record CreateClientRequest(

        @Schema(description = "Human-friendly reference, unique within the company. Auto-generated (e.g. CLI-0001) when omitted.",
                example = "CLI-0001", nullable = true)
        @Size(max = 32)
        String reference,

        @Schema(description = "Whether the client is a business or an individual", example = "COMPANY", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        ClientType type,

        @Schema(description = "Display name (company trade name or person's full name)", example = "ACME Logistics", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        @Size(max = 200)
        String name,

        @Schema(description = "Legal identification (mostly relevant for COMPANY clients). All fields optional.", nullable = true)
        @Valid
        LegalInfoDto legalInfo,

        @Schema(description = "Main e-mail address of the client", example = "contact@acme.example", nullable = true)
        @Email
        @Size(max = 255)
        String email,

        @Schema(description = "Main phone number of the client", example = "+33123456789", nullable = true)
        @Size(max = 32)
        String phone,

        @Schema(description = "Website URL of the client", example = "https://acme.example", nullable = true)
        @Size(max = 255)
        String website,

        @Schema(description = "Default payment term in days, used as the invoice due delay. Defaults to 30.",
                example = "30", defaultValue = "30", nullable = true, minimum = "0", maximum = "365")
        @Min(0)
        @Max(365)
        Integer paymentTermsDays,

        @Schema(description = "Identifier of the company member (its user id) who manages this client", example = "8a1f2c3b-4d5e-6f70-3f1c-8d2e9b4a4d6e", nullable = true)
        UUID accountManagerUserId,

        @Schema(description = "Free-form internal notes about the client", example = "Key account, invoices reviewed monthly.", nullable = true)
        String notes,

        @Schema(description = "Per-client preferences (automatic e-mails, language). Defaults applied when omitted.", nullable = true)
        @Valid
        ClientSettingsDto settings
) {
}
