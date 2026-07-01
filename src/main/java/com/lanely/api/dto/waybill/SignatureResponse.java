package com.lanely.api.dto.waybill;

import com.lanely.api.entity.enums.SignatureMethod;
import com.lanely.api.entity.enums.WaybillPartyRole;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(name = "SignatureResponse", description = "An electronic signature captured on the waybill")
public record SignatureResponse(

        @Schema(description = "Signature identifier", example = "5e6f7a8b-9c0d-1e2f-3a4b-5c6d7e8f9a0b")
        UUID id,

        @Schema(description = "Role of the signing party", example = "CONSIGNEE")
        WaybillPartyRole role,

        @Schema(description = "Name of the person who signed", example = "Jean Martin")
        String signerName,

        @Schema(type = "string", format = "date-time", description = "Signing instant (ISO-8601 UTC)", example = "2026-06-23T15:42:00Z")
        Instant signedAt,

        @Schema(description = "Place of signature", example = "Rennes", nullable = true)
        String place,

        @Schema(description = "How the signature was captured", example = "DRAWN")
        SignatureMethod method,

        @Schema(description = "Relative URL of the signature image, if any (prepend the API base URL)", example = "/images/7a8b9c0d-...", nullable = true)
        String signatureImageUrl
) {
}
