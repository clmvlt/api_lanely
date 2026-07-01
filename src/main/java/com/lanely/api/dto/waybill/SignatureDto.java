package com.lanely.api.dto.waybill;

import com.lanely.api.entity.enums.SignatureMethod;
import com.lanely.api.entity.enums.WaybillPartyRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(name = "SignatureDto", description = "An electronic signature (eCMR) captured on the waybill. The signing instant is set by the server.")
public record SignatureDto(

        @Schema(description = "Role of the signing party. Only SHIPPER and CONSIGNEE are accepted; CARRIER is rejected with 400.",
                example = "CONSIGNEE", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        WaybillPartyRole role,

        @Schema(description = "Name of the person who signed", example = "Jean Martin", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        @Size(max = 150)
        String signerName,

        @Schema(description = "Place of signature", example = "Rennes", nullable = true)
        @Size(max = 200)
        String place,

        @Schema(description = "How the signature was captured", example = "DRAWN", defaultValue = "CLICKWRAP", nullable = true)
        SignatureMethod method
) {
}
