package com.lanely.api.dto.waybill;

import com.lanely.api.dto.company.AddressDto;
import com.lanely.api.dto.company.LegalInfoDto;
import com.lanely.api.dto.geo.CoordinateDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Schema(name = "WaybillPartyDto", description = "A party of the waybill (shipper or consignee). A frozen snapshot: it can either be filled "
        + "freely (one-off / 'random' party) or pre-filled from an existing client by setting clientId (and optionally clientAddressId).")
public record WaybillPartyDto(

        @Schema(description = "Display name of the party", example = "ACME Logistics", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        @Size(max = 200)
        String name,

        @Schema(description = "Postal address", nullable = true)
        @Valid
        AddressDto address,

        @Schema(description = "GPS coordinates (used for routing). When omitted, the server geocodes the address.", nullable = true)
        @Valid
        CoordinateDto location,

        @Schema(description = "On-site contact name", example = "Marie Durand", nullable = true)
        @Size(max = 150)
        String contactName,

        @Schema(description = "On-site contact phone", example = "+33123456789", nullable = true)
        @Size(max = 32)
        String contactPhone,

        @Schema(description = "On-site contact e-mail", example = "ops@acme.example", nullable = true)
        @Email
        @Size(max = 255)
        String contactEmail,

        @Schema(description = "Legal identification (CMR international)", nullable = true)
        @Valid
        LegalInfoDto legalInfo,

        @Schema(description = "Optional link to an existing client to pre-fill the snapshot", example = "9f1c2d3e-4a5b-6c7d-8e9f-0a1b2c3d4e5f", nullable = true)
        UUID clientId,

        @Schema(description = "Optional link to a specific client address to pre-fill the snapshot", example = "1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d", nullable = true)
        UUID clientAddressId
) {
}
