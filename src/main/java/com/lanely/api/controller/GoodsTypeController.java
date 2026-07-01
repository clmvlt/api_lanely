package com.lanely.api.controller;

import com.lanely.api.dto.common.PageResponse;
import com.lanely.api.dto.error.ErrorResponse;
import com.lanely.api.dto.goodstype.CreateGoodsTypeRequest;
import com.lanely.api.dto.goodstype.GoodsTypeResponse;
import com.lanely.api.dto.goodstype.UpdateGoodsTypeRequest;
import com.lanely.api.security.AuthenticatedUser;
import com.lanely.api.service.GoodsTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/companies/{companyId}/goods-types")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Goods types", description = "Manage a company's catalog of reusable goods/cargo types used for autocompletion when filling waybill goods lines. "
        + "Goods types are decoupled from waybills: deleting one never affects existing waybills.")
public class GoodsTypeController {

    private final GoodsTypeService goodsTypeService;

    public GoodsTypeController(GoodsTypeService goodsTypeService) {
        this.goodsTypeService = goodsTypeService;
    }

    @PostMapping
    @Operation(
            summary = "Create a goods type",
            description = "Creates a reusable goods type in the company's catalog. Requires the MANAGE_TRANSPORTS permission (the OWNER always has it). "
                    + "The name is unique within the company (case-insensitive). Only name is required; all other fields are optional default "
                    + "values that the front pre-fills when the type is selected while editing a waybill goods line."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Goods type created",
                    content = @Content(schema = @Schema(implementation = GoodsTypeResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed for one or more fields",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller lacks the MANAGE_TRANSPORTS permission",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "A goods type with the same name already exists in this company",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<GoodsTypeResponse> createGoodsType(
            @Parameter(description = "Identifier of the company", example = "11112222-3333-4444-5555-666677778888")
            @PathVariable UUID companyId,
            @Valid @RequestBody CreateGoodsTypeRequest request,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        GoodsTypeResponse response = goodsTypeService.createGoodsType(principal.userId(), companyId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(
            summary = "List goods types",
            description = "Returns a paginated list of the company's goods types, suitable for feeding autocompletion. The caller must be a member "
                    + "of the company. Optionally filter by a free-text query matching the name or description. Pagination uses the standard page, "
                    + "size and sort query parameters (sortable on name, createdAt, updatedAt)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of goods types returned",
                    content = @Content(schema = @Schema(implementation = PageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Sorting requested on an unsupported field",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller is not a member of this company",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public PageResponse<GoodsTypeResponse> listGoodsTypes(
            @Parameter(description = "Identifier of the company", example = "11112222-3333-4444-5555-666677778888")
            @PathVariable UUID companyId,
            @Parameter(description = "Free-text search on name or description (case-insensitive)", example = "pallet")
            @RequestParam(required = false) String q,
            @ParameterObject Pageable pageable,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return goodsTypeService.listGoodsTypes(principal.userId(), companyId, q, pageable);
    }

    @GetMapping("/{goodsTypeId}")
    @Operation(
            summary = "Get a goods type",
            description = "Returns the full detail of a goods type, including its default values. The caller must be a member of the company."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Goods type returned",
                    content = @Content(schema = @Schema(implementation = GoodsTypeResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller is not a member of this company",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company or goods type not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public GoodsTypeResponse getGoodsType(
            @Parameter(description = "Identifier of the company", example = "11112222-3333-4444-5555-666677778888")
            @PathVariable UUID companyId,
            @Parameter(description = "Identifier of the goods type", example = "3a4b5c6d-7e8f-9a0b-1c2d-3e4f5a6b7c8d")
            @PathVariable UUID goodsTypeId,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return goodsTypeService.getGoodsType(principal.userId(), companyId, goodsTypeId);
    }

    @PatchMapping("/{goodsTypeId}")
    @Operation(
            summary = "Update a goods type",
            description = "Updates a goods type's editable information. Only non-null fields are applied. Requires the MANAGE_TRANSPORTS permission. "
                    + "A changed name must remain unique within the company (case-insensitive)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Goods type updated",
                    content = @Content(schema = @Schema(implementation = GoodsTypeResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed for one or more fields",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller lacks the MANAGE_TRANSPORTS permission",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company or goods type not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Another goods type already uses the new name",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public GoodsTypeResponse updateGoodsType(
            @Parameter(description = "Identifier of the company", example = "11112222-3333-4444-5555-666677778888")
            @PathVariable UUID companyId,
            @Parameter(description = "Identifier of the goods type", example = "3a4b5c6d-7e8f-9a0b-1c2d-3e4f5a6b7c8d")
            @PathVariable UUID goodsTypeId,
            @Valid @RequestBody UpdateGoodsTypeRequest request,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return goodsTypeService.updateGoodsType(principal.userId(), companyId, goodsTypeId, request);
    }

    @DeleteMapping("/{goodsTypeId}")
    @Operation(
            summary = "Delete a goods type",
            description = "Permanently deletes a goods type from the company's catalog. Requires the MANAGE_TRANSPORTS permission. "
                    + "Existing waybills are unaffected (goods types are decoupled from waybills)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Goods type deleted"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller lacks the MANAGE_TRANSPORTS permission",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company or goods type not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteGoodsType(
            @Parameter(description = "Identifier of the company", example = "11112222-3333-4444-5555-666677778888")
            @PathVariable UUID companyId,
            @Parameter(description = "Identifier of the goods type", example = "3a4b5c6d-7e8f-9a0b-1c2d-3e4f5a6b7c8d")
            @PathVariable UUID goodsTypeId,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        goodsTypeService.deleteGoodsType(principal.userId(), companyId, goodsTypeId);
        return ResponseEntity.noContent().build();
    }
}
