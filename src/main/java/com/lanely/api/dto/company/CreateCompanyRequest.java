package com.lanely.api.dto.company;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "CreateCompanyRequest", description = "Payload to create a new company owned by the calling user. "
        + "Only 'name' is required; legal and billing information are optional and can also be completed later via PATCH /companies/{companyId}.")
public record CreateCompanyRequest(

        @Schema(description = "Display name of the company", example = "Speedy Delivery", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        @Size(max = 150)
        String name,

        @Schema(description = "Optional legal identification of the company, used for invoicing", nullable = true)
        @Valid
        LegalInfoDto legalInfo,

        @Schema(description = "Optional billing/legal address. If omitted, country defaults to FR.", nullable = true)
        @Valid
        AddressDto billingAddress,

        @Schema(description = "Optional depot/warehouse address with manually provided GPS coordinates, separate from the billing address. "
                + "Coordinates are never geocoded automatically.", nullable = true)
        @Valid
        DepositAddressDto depositAddress,

        @Schema(description = "Optional billing contact email where invoices are sent", example = "billing@speedy-delivery.example", nullable = true)
        @Email
        @Size(max = 255)
        String billingEmail,

        @Schema(description = "Optional billing contact phone number (E.164 format recommended)", example = "+33123456789", nullable = true)
        @Size(max = 32)
        String billingPhone
) {
}
