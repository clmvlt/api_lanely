package com.lanely.api.dto.waybill;

import com.lanely.api.dto.company.AddressDto;
import com.lanely.api.dto.company.LegalInfoDto;
import com.lanely.api.dto.geo.CoordinateDto;
import com.lanely.api.entity.enums.WaybillPartyRole;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(name = "WaybillPartyResponse", description = "A party of the waybill (frozen snapshot)")
public record WaybillPartyResponse(

        @Schema(description = "Role of the party", example = "SHIPPER")
        WaybillPartyRole role,

        @Schema(description = "Display name", example = "ACME Logistics")
        String name,

        @Schema(description = "Postal address", nullable = true)
        AddressDto address,

        @Schema(description = "GPS coordinates", nullable = true)
        CoordinateDto location,

        @Schema(description = "On-site contact name", nullable = true)
        String contactName,

        @Schema(description = "On-site contact phone", nullable = true)
        String contactPhone,

        @Schema(description = "On-site contact e-mail", nullable = true)
        String contactEmail,

        @Schema(description = "Legal identification", nullable = true)
        LegalInfoDto legalInfo,

        @Schema(description = "Linked client id, if any", nullable = true)
        UUID clientId,

        @Schema(description = "Linked client address id, if any", nullable = true)
        UUID clientAddressId
) {
}
