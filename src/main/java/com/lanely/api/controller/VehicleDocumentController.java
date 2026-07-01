package com.lanely.api.controller;

import com.lanely.api.dto.error.ErrorResponse;
import com.lanely.api.dto.vehicle.VehicleDocumentResponse;
import com.lanely.api.entity.enums.VehicleDocumentCategory;
import com.lanely.api.security.AuthenticatedUser;
import com.lanely.api.service.LoadedDocument;
import com.lanely.api.service.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/companies/{companyId}/vehicles/{vehicleId}/documents")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Vehicle documents", description = "Manage the documents and photos attached to a vehicle (registration card, insurance, photos)")
public class VehicleDocumentController {

    private final VehicleService vehicleService;

    public VehicleDocumentController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @GetMapping
    @Operation(
            summary = "List vehicle documents",
            description = "Returns all documents and photos attached to the vehicle, most recent first. The caller must be a member of the "
                    + "company. Optionally filter by category. Each item carries a relative downloadUrl to fetch the file content."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Documents returned",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = VehicleDocumentResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller is not a member of this company",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company or vehicle not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public List<VehicleDocumentResponse> listDocuments(
            @Parameter(description = "Identifier of the company", example = "11112222-3333-4444-5555-666677778888")
            @PathVariable UUID companyId,
            @Parameter(description = "Identifier of the vehicle", example = "9f1c2d3e-4a5b-6c7d-8e9f-0a1b2c3d4e5f")
            @PathVariable UUID vehicleId,
            @Parameter(description = "Filter by document category. Omit to return documents of any category.", example = "REGISTRATION_CARD")
            @RequestParam(required = false) VehicleDocumentCategory category,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return vehicleService.listDocuments(principal.userId(), companyId, vehicleId, category);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Upload a vehicle document",
            description = "Uploads a document or photo and attaches it to the vehicle via multipart/form-data. Requires the MANAGE_VEHICLES "
                    + "permission. Accepted types: PDF, PNG, JPEG, WEBP (max 10 MB). The 'category' classifies the file "
                    + "(REGISTRATION_CARD, INSURANCE, PHOTO, OTHER) and an optional 'label' adds a human-friendly name."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Document uploaded",
                    content = @Content(schema = @Schema(implementation = VehicleDocumentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Missing/invalid/too large file, or unsupported content type",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller lacks the MANAGE_VEHICLES permission",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company or vehicle not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<VehicleDocumentResponse> uploadDocument(
            @Parameter(description = "Identifier of the company", example = "11112222-3333-4444-5555-666677778888")
            @PathVariable UUID companyId,
            @Parameter(description = "Identifier of the vehicle", example = "9f1c2d3e-4a5b-6c7d-8e9f-0a1b2c3d4e5f")
            @PathVariable UUID vehicleId,
            @Parameter(description = "Document category", example = "REGISTRATION_CARD")
            @RequestParam VehicleDocumentCategory category,
            @Parameter(description = "Optional human-friendly label", example = "Registration card 2026")
            @RequestParam(required = false) String label,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        VehicleDocumentResponse response = vehicleService.uploadDocument(principal.userId(), companyId, vehicleId, category, label, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{documentId}/download")
    @Operation(
            summary = "Download a vehicle document",
            description = "Streams the raw bytes of an attached document with its content type, as an attachment. The caller must be a member "
                    + "of the company. Responses are privately cacheable."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Document bytes returned",
                    content = @Content(mediaType = "application/octet-stream")),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller is not a member of this company",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company, vehicle or document not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Resource> downloadDocument(
            @Parameter(description = "Identifier of the company", example = "11112222-3333-4444-5555-666677778888")
            @PathVariable UUID companyId,
            @Parameter(description = "Identifier of the vehicle", example = "9f1c2d3e-4a5b-6c7d-8e9f-0a1b2c3d4e5f")
            @PathVariable UUID vehicleId,
            @Parameter(description = "Identifier of the vehicle document", example = "1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d")
            @PathVariable UUID documentId,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        LoadedDocument document = vehicleService.getDocumentContent(principal.userId(), companyId, vehicleId, documentId);
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(document.originalFilename() == null ? "document" : document.originalFilename())
                .build();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(document.contentType()))
                .contentLength(document.sizeBytes())
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .cacheControl(CacheControl.maxAge(java.time.Duration.ofDays(1)).cachePrivate())
                .body(document.resource());
    }

    @DeleteMapping("/{documentId}")
    @Operation(
            summary = "Delete a vehicle document",
            description = "Permanently deletes a document attached to the vehicle and its stored content. Requires the MANAGE_VEHICLES permission."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Document deleted"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller lacks the MANAGE_VEHICLES permission",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company, vehicle or document not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteDocument(
            @Parameter(description = "Identifier of the company", example = "11112222-3333-4444-5555-666677778888")
            @PathVariable UUID companyId,
            @Parameter(description = "Identifier of the vehicle", example = "9f1c2d3e-4a5b-6c7d-8e9f-0a1b2c3d4e5f")
            @PathVariable UUID vehicleId,
            @Parameter(description = "Identifier of the vehicle document", example = "1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d")
            @PathVariable UUID documentId,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        vehicleService.deleteDocument(principal.userId(), companyId, vehicleId, documentId);
        return ResponseEntity.noContent().build();
    }
}
