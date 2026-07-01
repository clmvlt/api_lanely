package com.lanely.api.dto.vehicle;

import com.lanely.api.entity.enums.FuelType;
import com.lanely.api.entity.enums.VehicleStatus;
import com.lanely.api.entity.enums.VehicleType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Schema(name = "VehicleResponse", description = "A vehicle of the transport company's fleet, with its documents")
public record VehicleResponse(

        @Schema(description = "Unique vehicle identifier", example = "9f1c2d3e-4a5b-6c7d-8e9f-0a1b2c3d4e5f")
        UUID id,

        @Schema(description = "Registration (license) plate, unique within the company", example = "AB-123-CD")
        String registrationPlate,

        @Schema(description = "Vehicle identification number (VIN / chassis number)", example = "VF1RFB00256123456", nullable = true)
        String vin,

        @Schema(description = "Manufacturer (make)", example = "Renault", nullable = true)
        String make,

        @Schema(description = "Model", example = "Master", nullable = true)
        String model,

        @Schema(description = "Version / trim", example = "L2H2 dCi 135", nullable = true)
        String version,

        @Schema(description = "Vehicle category", example = "TRUCK")
        VehicleType vehicleType,

        @Schema(description = "Fuel type", example = "DIESEL", nullable = true)
        FuelType fuelType,

        @Schema(type = "string", format = "date", description = "Date of first registration (ISO-8601, civil date)", example = "2021-03-15", nullable = true)
        LocalDate firstRegistrationDate,

        @Schema(description = "Emission class (e.g. Euro 6, Crit'Air 1)", example = "Euro 6", nullable = true)
        String emissionClass,

        @Schema(description = "Gross vehicle weight rating in kilograms (PTAC)", example = "3500", nullable = true)
        Integer grossWeightKg,

        @Schema(description = "Payload capacity in kilograms", example = "1200", nullable = true)
        Integer payloadKg,

        @Schema(description = "Registration certificate number (carte grise)", example = "2021AB12345", nullable = true)
        String registrationCertificateNumber,

        @Schema(description = "Insurance details", nullable = true)
        InsuranceInfoDto insurance,

        @Schema(type = "string", format = "date", description = "Next technical inspection date (ISO-8601, civil date)", example = "2027-03-15", nullable = true)
        LocalDate technicalInspectionDate,

        @Schema(type = "string", format = "date", description = "Road tax due date (ISO-8601, civil date)", example = "2027-01-01", nullable = true)
        LocalDate roadTaxDueDate,

        @Schema(description = "Latest recorded mileage in kilometers", example = "84210", nullable = true)
        Integer latestMileageKm,

        @Schema(type = "string", format = "date-time", description = "Instant of the latest recorded mileage (ISO-8601 UTC)", example = "2026-06-10T09:15:30Z", nullable = true)
        Instant latestMileageAt,

        @Schema(description = "Lifecycle status", example = "ACTIVE")
        VehicleStatus status,

        @Schema(description = "Free-form internal notes", example = "Spare key in the office.", nullable = true)
        String notes,

        @Schema(description = "Attached documents and photos")
        List<VehicleDocumentResponse> documents,

        @Schema(type = "string", format = "date-time", description = "Creation instant (ISO-8601 UTC)", example = "2026-06-10T09:15:30Z")
        Instant createdAt,

        @Schema(type = "string", format = "date-time", description = "Last update instant (ISO-8601 UTC)", example = "2026-06-10T09:15:30Z")
        Instant updatedAt
) {
}
