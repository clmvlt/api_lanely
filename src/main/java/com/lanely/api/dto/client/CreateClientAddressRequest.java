package com.lanely.api.dto.client;

import com.lanely.api.dto.company.AddressDto;
import com.lanely.api.entity.enums.AddressType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

@Schema(name = "CreateClientAddressRequest", description = "Payload to add an address to a client. "
        + "The address country defaults to the company's country when omitted. When latitude/longitude are "
        + "omitted, they are geocoded automatically from the postal address (best-effort: left null if the "
        + "address cannot be located).")
public record CreateClientAddressRequest(

        @Schema(description = "Human-friendly label", example = "North depot", nullable = true)
        @Size(max = 120)
        String label,

        @Schema(description = "Address category. Defaults to DEPOT.", example = "DEPOT", defaultValue = "DEPOT", nullable = true)
        AddressType type,

        @Schema(description = "Postal address. Country defaults to the company's country when omitted.", nullable = true)
        @Valid
        AddressDto address,

        @Schema(description = "Latitude (WGS84, decimal degrees). Geocoded automatically from the postal address when omitted.", example = "48.8566", nullable = true, minimum = "-90", maximum = "90")
        @DecimalMin("-90.0")
        @DecimalMax("90.0")
        Double latitude,

        @Schema(description = "Longitude (WGS84, decimal degrees). Geocoded automatically from the postal address when omitted.", example = "2.3522", nullable = true, minimum = "-180", maximum = "180")
        @DecimalMin("-180.0")
        @DecimalMax("180.0")
        Double longitude,

        @Schema(description = "Mark this address as the client's primary address (unsets the flag on other addresses)", example = "false", nullable = true)
        Boolean isPrimary,

        @Schema(description = "Mark this address as the default billing address (unsets the flag on other addresses)", example = "false", nullable = true)
        Boolean isDefaultBilling,

        @Schema(description = "Mark this address as the default shipping address (unsets the flag on other addresses)", example = "true", nullable = true)
        Boolean isDefaultShipping,

        @Schema(description = "On-site contact name", example = "Marie Dupont", nullable = true)
        @Size(max = 150)
        String contactName,

        @Schema(description = "On-site contact phone", example = "+33123456789", nullable = true)
        @Size(max = 32)
        String contactPhone,

        @Schema(description = "On-site contact e-mail", example = "depot.north@acme.example", nullable = true)
        @Email
        @Size(max = 255)
        String contactEmail,

        @Schema(description = "Access notes / delivery instructions for drivers", example = "Ring at gate B, trucks under 12m only.", nullable = true)
        String deliveryInstructions
) {
}
