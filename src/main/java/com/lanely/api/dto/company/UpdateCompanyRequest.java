package com.lanely.api.dto.company;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "UpdateCompanyRequest", description = "Payload to update a company's editable information. This is a full replacement of "
        + "the editable fields: 'name' is required, and any optional legal/billing field left null is cleared. Send the complete desired state.")
public record UpdateCompanyRequest(

        @Schema(description = "New display name of the company", example = "Speedy Delivery Pro", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        @Size(max = 150)
        String name,

        @Schema(description = "Legal identification of the company, used for invoicing. Null clears all legal fields.", nullable = true)
        @Valid
        LegalInfoDto legalInfo,

        @Schema(description = "Billing/legal address. Null clears the address (country resets to its FR default).", nullable = true)
        @Valid
        AddressDto billingAddress,

        @Schema(description = "Depot/warehouse address with manually provided GPS coordinates, separate from the billing address. "
                + "Null clears both the depot address and its coordinates. Coordinates are never geocoded automatically.", nullable = true)
        @Valid
        DepositAddressDto depositAddress,

        @Schema(description = "Billing contact email where invoices are sent. Null clears it.", example = "billing@speedy-delivery.example", nullable = true)
        @Email
        @Size(max = 255)
        String billingEmail,

        @Schema(description = "Billing contact phone number (E.164 format recommended). Null clears it.", example = "+33123456789", nullable = true)
        @Size(max = 32)
        String billingPhone
) {
}
