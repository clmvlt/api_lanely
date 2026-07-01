package com.lanely.api.dto.vehicle;

import com.lanely.api.entity.enums.FuelType;
import com.lanely.api.entity.enums.VehicleType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(name = "UpdateVehicleRequest", description = "Payload to update a vehicle. Only non-null fields are applied.")
public record UpdateVehicleRequest(

        @Schema(description = "Registration (license) plate, unique within the company", example = "AB-123-CD", nullable = true)
        @Size(max = 16)
        String registrationPlate,

        @Schema(description = "Vehicle identification number (VIN / chassis number)", example = "VF1RFB00256123456", nullable = true)
        @Size(max = 17)
        String vin,

        @Schema(description = "Manufacturer (make)", example = "Renault", nullable = true)
        @Size(max = 64)
        String make,

        @Schema(description = "Model", example = "Master", nullable = true)
        @Size(max = 64)
        String model,

        @Schema(description = "Version / trim", example = "L2H2 dCi 135", nullable = true)
        @Size(max = 128)
        String version,

        @Schema(description = "Vehicle category", example = "TRUCK", nullable = true)
        VehicleType vehicleType,

        @Schema(description = "Fuel type", example = "DIESEL", nullable = true)
        FuelType fuelType,

        @Schema(type = "string", format = "date", description = "Date of first registration (ISO-8601, civil date)", example = "2021-03-15", nullable = true)
        LocalDate firstRegistrationDate,

        @Schema(description = "Emission class (e.g. Euro 6, Crit'Air 1)", example = "Euro 6", nullable = true)
        @Size(max = 32)
        String emissionClass,

        @Schema(description = "Gross vehicle weight rating in kilograms (PTAC)", example = "3500", nullable = true, minimum = "0")
        @Min(0)
        Integer grossWeightKg,

        @Schema(description = "Payload capacity in kilograms", example = "1200", nullable = true, minimum = "0")
        @Min(0)
        Integer payloadKg,

        @Schema(description = "Registration certificate number (carte grise)", example = "2021AB12345", nullable = true)
        @Size(max = 64)
        String registrationCertificateNumber,

        @Schema(description = "Insurance details. Merged field by field.", nullable = true)
        @Valid
        InsuranceInfoDto insurance,

        @Schema(type = "string", format = "date", description = "Next technical inspection date (ISO-8601, civil date)", example = "2027-03-15", nullable = true)
        LocalDate technicalInspectionDate,

        @Schema(type = "string", format = "date", description = "Road tax due date (ISO-8601, civil date)", example = "2027-01-01", nullable = true)
        LocalDate roadTaxDueDate,

        @Schema(description = "Free-form internal notes about the vehicle", example = "Spare key in the office.", nullable = true)
        String notes
) {
}
