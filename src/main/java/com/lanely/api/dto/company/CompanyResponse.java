package com.lanely.api.dto.company;

import com.lanely.api.entity.enums.CompanyRole;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(name = "CompanyResponse", description = "A company as seen by one of its members")
public record CompanyResponse(

        @Schema(description = "Unique company identifier", example = "11112222-3333-4444-5555-666677778888")
        UUID id,

        @Schema(description = "Company display name", example = "Speedy Delivery")
        String name,

        @Schema(description = "Public company code (used by mobile profiles to link before login)", example = "K7P2M9QX")
        String publicCode,

        @Schema(description = "Role of the calling user within this company", example = "OWNER")
        CompanyRole callerRole,

        @Schema(description = "Relative URL of the company profile picture, or null if none", example = "/images/aaaa1111-bbbb-2222-cccc-3333dddd4444", nullable = true)
        String profileImageUrl,

        @Schema(description = "Legal identification of the company (for invoicing). Fields may be null if not set yet.", nullable = true)
        LegalInfoDto legalInfo,

        @Schema(description = "Billing/legal address. Fields may be null if not set yet; country defaults to FR.", nullable = true)
        AddressDto billingAddress,

        @Schema(description = "Depot/warehouse address with its manually provided GPS coordinates, separate from the billing address. "
                + "Fields may be null if not set yet.", nullable = true)
        DepositAddressDto depositAddress,

        @Schema(description = "Billing contact email where invoices are sent, or null if not set", example = "billing@speedy-delivery.example", nullable = true)
        String billingEmail,

        @Schema(description = "Billing contact phone number, or null if not set", example = "+33123456789", nullable = true)
        String billingPhone
) {
}
