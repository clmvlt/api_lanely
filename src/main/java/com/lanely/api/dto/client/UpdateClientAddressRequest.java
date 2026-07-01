package com.lanely.api.dto.client;

import com.lanely.api.dto.company.AddressDto;
import com.lanely.api.entity.enums.AddressType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

@Schema(name = "UpdateClientAddressRequest", description = "Payload to update a client address. Only non-null fields are applied. "
        + "When the postal address is changed without providing latitude/longitude, coordinates are re-geocoded "
        + "automatically (best-effort).")
public record UpdateClientAddressRequest(

        @Schema(description = "New label. Null to keep unchanged.", example = "North depot", nullable = true)
        @Size(max = 120)
        String label,

        @Schema(description = "New address category. Null to keep unchanged.", example = "SHIPPING", nullable = true)
        AddressType type,

        @Schema(description = "New postal address (replaces the whole block when provided). Null to keep unchanged.", nullable = true)
        @Valid
        AddressDto address,

        @Schema(description = "New latitude. Null to keep unchanged (or to trigger re-geocoding when the address changes).", example = "48.8566", nullable = true, minimum = "-90", maximum = "90")
        @DecimalMin("-90.0")
        @DecimalMax("90.0")
        Double latitude,

        @Schema(description = "New longitude. Null to keep unchanged (or to trigger re-geocoding when the address changes).", example = "2.3522", nullable = true, minimum = "-180", maximum = "180")
        @DecimalMin("-180.0")
        @DecimalMax("180.0")
        Double longitude,

        @Schema(description = "Set/unset primary flag. Setting true unsets it on other addresses. Null to keep unchanged.", example = "true", nullable = true)
        Boolean isPrimary,

        @Schema(description = "Set/unset default billing flag. Setting true unsets it on other addresses. Null to keep unchanged.", example = "false", nullable = true)
        Boolean isDefaultBilling,

        @Schema(description = "Set/unset default shipping flag. Setting true unsets it on other addresses. Null to keep unchanged.", example = "true", nullable = true)
        Boolean isDefaultShipping,

        @Schema(description = "New on-site contact name. Null to keep unchanged.", example = "Marie Dupont", nullable = true)
        @Size(max = 150)
        String contactName,

        @Schema(description = "New on-site contact phone. Null to keep unchanged.", example = "+33123456789", nullable = true)
        @Size(max = 32)
        String contactPhone,

        @Schema(description = "New on-site contact e-mail. Null to keep unchanged.", example = "depot.north@acme.example", nullable = true)
        @Email
        @Size(max = 255)
        String contactEmail,

        @Schema(description = "New delivery instructions. Null to keep unchanged.", example = "Ring at gate B.", nullable = true)
        String deliveryInstructions,

        @Schema(description = "Activate/deactivate the address. Null to keep unchanged.", example = "true", nullable = true)
        Boolean active
) {
}
