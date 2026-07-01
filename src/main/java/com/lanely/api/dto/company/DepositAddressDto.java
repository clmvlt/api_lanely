package com.lanely.api.dto.company;

import com.lanely.api.dto.geo.CoordinateDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;

@Schema(name = "DepositAddressDto", description = "Warehouse / depot address where parcels are stored or picked up, separate from the billing address. "
        + "The postal address and the GPS coordinates are both optional and independent: coordinates are supplied manually through the API and are never "
        + "geocoded automatically from the address.")
public record DepositAddressDto(

        @Schema(description = "Postal address of the depot. All fields optional; country defaults to FR when omitted.", nullable = true)
        @Valid
        AddressDto address,

        @Schema(description = "Manually provided WGS84 GPS coordinates of the depot. Both latitude and longitude must be set together, or left null. "
                + "Never computed automatically from the address.", nullable = true)
        @Valid
        CoordinateDto coordinate
) {
}
