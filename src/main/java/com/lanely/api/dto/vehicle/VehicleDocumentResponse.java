package com.lanely.api.dto.vehicle;

import com.lanely.api.entity.enums.VehicleDocumentCategory;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(name = "VehicleDocumentResponse", description = "A document or photo attached to a vehicle")
public record VehicleDocumentResponse(

        @Schema(description = "Unique vehicle document identifier", example = "1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d")
        UUID id,

        @Schema(description = "Document category", example = "REGISTRATION_CARD")
        VehicleDocumentCategory category,

        @Schema(description = "Optional human-friendly label", example = "Registration card 2026", nullable = true)
        String label,

        @Schema(description = "Original file name as uploaded", example = "carte-grise.pdf", nullable = true)
        String originalFilename,

        @Schema(description = "MIME content type of the stored file", example = "application/pdf")
        String contentType,

        @Schema(description = "Size of the stored file in bytes", example = "248173")
        long sizeBytes,

        @Schema(description = "Relative URL to download the document content", example = "/companies/11112222-3333-4444-5555-666677778888/vehicles/9f1c2d3e-4a5b-6c7d-8e9f-0a1b2c3d4e5f/documents/1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d/download")
        String downloadUrl,

        @Schema(description = "Account id of the member who uploaded the document", example = "8a1f2c3b-4d5e-6f70-3f1c-8d2e9b4a4d6e", nullable = true)
        UUID uploadedByUserId,

        @Schema(type = "string", format = "date-time", description = "Creation instant (ISO-8601 UTC)", example = "2026-06-10T09:15:30Z")
        Instant createdAt
) {
}
