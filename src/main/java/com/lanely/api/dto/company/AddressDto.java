package com.lanely.api.dto.company;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(name = "AddressDto", description = "Postal address used for billing and legal documents. All fields are optional; "
        + "country is an ISO 3166-1 alpha-2 code and defaults to FR when omitted.")
public record AddressDto(

        @Schema(description = "First address line (street and number)", example = "12 Rue de Rivoli", nullable = true)
        @Size(max = 200)
        String line1,

        @Schema(description = "Second address line (building, suite, floor, etc.)", example = "Building B, 3rd floor", nullable = true)
        @Size(max = 200)
        String line2,

        @Schema(description = "Postal / ZIP code", example = "75001", nullable = true)
        @Size(max = 20)
        String postalCode,

        @Schema(description = "City", example = "Paris", nullable = true)
        @Size(max = 120)
        String city,

        @Schema(description = "State, region or province", example = "Ile-de-France", nullable = true)
        @Size(max = 120)
        String state,

        @Schema(description = "Country as an ISO 3166-1 alpha-2 code (case-insensitive on input, stored uppercase). Defaults to FR when omitted.",
                example = "FR", defaultValue = "FR", nullable = true)
        @Pattern(regexp = "^[A-Za-z]{2}$", message = "{validation.company.country}")
        String country
) {
}
