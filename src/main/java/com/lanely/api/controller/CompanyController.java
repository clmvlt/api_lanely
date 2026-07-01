package com.lanely.api.controller;

import com.lanely.api.dto.company.CompanyCodeResponse;
import com.lanely.api.dto.company.CompanyMeResponse;
import com.lanely.api.dto.company.CompanyResponse;
import com.lanely.api.dto.company.CreateCompanyRequest;
import com.lanely.api.dto.company.PublicCompanyResponse;
import com.lanely.api.dto.company.UpdateCompanyRequest;
import com.lanely.api.dto.error.ErrorResponse;
import com.lanely.api.dto.member.MemberResponse;
import com.lanely.api.dto.permission.MemberPermissionsResponse;
import com.lanely.api.dto.permission.PermissionDto;
import com.lanely.api.dto.permission.UpdateMemberPermissionsRequest;
import com.lanely.api.dto.subscription.CompanySeatUsage;
import com.lanely.api.security.AuthenticatedUser;
import com.lanely.api.service.CompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/companies")
@Tag(name = "Companies", description = "Create and read companies, expose the public company code and list members")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Create a company",
            description = "Creates a new company. The calling web user becomes its OWNER with full rights and a membership with role OWNER "
                    + "is created. A unique public company code is generated for mobile profiles to link before login. "
                    + "Only 'name' is required. Optional legal identification (legalInfo) and billing details (billingAddress, billingEmail, "
                    + "billingPhone) may be supplied now or completed later via PATCH /companies/{companyId}; these are stored for invoicing "
                    + "and do not affect the API language. If no billing address country is provided, it defaults to FR. "
                    + "Creating a company requires an active subscription plan: a user with no plan cannot create any company, and a user "
                    + "who already owns as many companies as their plan allows cannot create another (see GET /subscription-plans for the "
                    + "per-plan company limit). The subscription only constrains the owner; invited members never need one."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Company created",
                    content = @Content(schema = @Schema(implementation = CompanyResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed for one or more fields",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "The caller has no subscription plan, or their plan's company limit is reached",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<CompanyResponse> createCompany(@Valid @RequestBody CreateCompanyRequest request,
                                                         @AuthenticationPrincipal AuthenticatedUser principal) {
        CompanyResponse response = companyService.createCompany(principal.userId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{companyId}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Get a company",
            description = "Returns a company with its full information as seen by a member: display name, public code, the caller's role, "
                    + "profile picture URL, and the legal/billing details (legalInfo, billingAddress, billingEmail, billingPhone). "
                    + "The caller must be a member of the company. Use this to populate the company settings and billing screens."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Company returned",
                    content = @Content(schema = @Schema(implementation = CompanyResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller is not a member of this company",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public CompanyResponse getCompany(
            @Parameter(description = "Identifier of the company", example = "11112222-3333-4444-5555-666677778888")
            @PathVariable UUID companyId,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return companyService.getCompany(principal.userId(), companyId);
    }

    @GetMapping("/{companyId}/code")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Get the public company code",
            description = "Returns the public code of a company. The caller must be a member of the company. This code is what mobile "
                    + "profiles scan to link to the company before logging in."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Company code returned",
                    content = @Content(schema = @Schema(implementation = CompanyCodeResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller is not a member of this company",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public CompanyCodeResponse getCompanyCode(
            @Parameter(description = "Identifier of the company", example = "11112222-3333-4444-5555-666677778888")
            @PathVariable UUID companyId,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return companyService.getCompanyCode(principal.userId(), companyId);
    }

    @GetMapping("/{companyId}/seats")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Get the company's seat usage",
            description = "Returns how many seats the company currently uses and how many it may use. A seat is one active mobile profile or "
                    + "one web member (the owner counts as a member); a deactivated profile frees its seat. The limit comes from the owner's "
                    + "subscription plan. Use this to display remaining capacity before creating a profile or inviting a member. The caller "
                    + "must be a member of the company."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Seat usage returned",
                    content = @Content(schema = @Schema(implementation = CompanySeatUsage.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller is not a member of this company",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public CompanySeatUsage getSeatUsage(
            @Parameter(description = "Identifier of the company", example = "11112222-3333-4444-5555-666677778888")
            @PathVariable UUID companyId,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return companyService.getSeatUsage(principal.userId(), companyId);
    }

    @GetMapping("/by-code/{publicCode}")
    @Operation(
            summary = "Resolve a company by its public code (mobile scan step)",
            description = "Public endpoint used by the mobile app after scanning a company code. Returns non-sensitive company information "
                    + "so the app can confirm the company before a profile logs in. No authentication required."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Company resolved",
                    content = @Content(schema = @Schema(implementation = PublicCompanyResponse.class))),
            @ApiResponse(responseCode = "404", description = "No company found for this code",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public PublicCompanyResponse getByPublicCode(
            @Parameter(description = "Public company code obtained by scanning", example = "K7P2M9QX")
            @PathVariable String publicCode) {
        return companyService.getByPublicCode(publicCode);
    }

    @GetMapping("/{companyId}/members")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "List company members",
            description = "Returns the web users that are members of the company together with their role (OWNER or MEMBER). "
                    + "The caller must be a member of the company."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Members returned",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = MemberResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller is not a member of this company",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public List<MemberResponse> listMembers(
            @Parameter(description = "Identifier of the company", example = "11112222-3333-4444-5555-666677778888")
            @PathVariable UUID companyId,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return companyService.listMembers(principal.userId(), companyId);
    }

    @PutMapping(value = "/{companyId}/picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Set the company profile picture",
            description = "Uploads (or replaces) the company's profile picture via multipart/form-data (field 'file'). Requires the "
                    + "MANAGE_COMPANY permission. Accepted types: PNG, JPEG, WEBP, GIF. Returns the company with its 'profileImageUrl'. "
                    + "Any previous picture is deleted."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Picture set",
                    content = @Content(schema = @Schema(implementation = CompanyResponse.class))),
            @ApiResponse(responseCode = "400", description = "Missing/invalid/too large file",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller lacks the MANAGE_COMPANY permission",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public CompanyResponse setCompanyPicture(
            @Parameter(description = "Identifier of the company", example = "11112222-3333-4444-5555-666677778888")
            @PathVariable UUID companyId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return companyService.setCompanyPicture(principal.userId(), companyId, file);
    }

    @DeleteMapping("/{companyId}/picture")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Remove the company profile picture",
            description = "Removes the company's profile picture (and deletes the stored image). Requires the MANAGE_COMPANY permission. "
                    + "No-op if there is none."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Picture removed",
                    content = @Content(schema = @Schema(implementation = CompanyResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller lacks the MANAGE_COMPANY permission",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public CompanyResponse removeCompanyPicture(
            @Parameter(description = "Identifier of the company", example = "11112222-3333-4444-5555-666677778888")
            @PathVariable UUID companyId,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return companyService.removeCompanyPicture(principal.userId(), companyId);
    }

    @PatchMapping("/{companyId}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Update company information",
            description = "Updates the editable information of a company: its display name plus legal identification (legalInfo) and billing "
                    + "details (billingAddress, billingEmail, billingPhone) used for invoicing. Requires the MANAGE_COMPANY permission "
                    + "(the OWNER always has it). This is a full replacement of the editable fields: 'name' is required, and any optional "
                    + "legal/billing field left null is cleared (a null billingAddress resets the country to its FR default). Send the "
                    + "complete desired state."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Company updated",
                    content = @Content(schema = @Schema(implementation = CompanyResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed for one or more fields",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller lacks the MANAGE_COMPANY permission",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public CompanyResponse updateCompany(
            @Parameter(description = "Identifier of the company", example = "11112222-3333-4444-5555-666677778888")
            @PathVariable UUID companyId,
            @Valid @RequestBody UpdateCompanyRequest request,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return companyService.updateCompany(principal.userId(), companyId, request);
    }

    @GetMapping("/{companyId}/me")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Get my role and permissions in a company",
            description = "Returns the calling user's role and effective permissions within the company (an OWNER has all permissions). "
                    + "Useful for the frontend to decide which actions to display."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Caller membership returned",
                    content = @Content(schema = @Schema(implementation = CompanyMeResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller is not a member of this company",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public CompanyMeResponse getMe(
            @Parameter(description = "Identifier of the company", example = "11112222-3333-4444-5555-666677778888")
            @PathVariable UUID companyId,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return companyService.getMe(principal.userId(), companyId);
    }

    @GetMapping("/permissions")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "List available permissions (catalog)",
            description = "Returns the full catalog of permissions that can be granted to company members, with their stable key and a "
                    + "human-readable description. The catalog can grow over time, so clients should not hard-code the list."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Permission catalog returned",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = PermissionDto.class)))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public List<PermissionDto> permissionCatalog() {
        return companyService.permissionCatalog();
    }

    @GetMapping("/{companyId}/members/{userId}/permissions")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Get a member's permissions",
            description = "Returns the effective permissions of a given member. Requires the MANAGE_PERMISSIONS permission (or OWNER)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Member permissions returned",
                    content = @Content(schema = @Schema(implementation = MemberPermissionsResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller lacks the MANAGE_PERMISSIONS permission",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company or member not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public MemberPermissionsResponse getMemberPermissions(
            @Parameter(description = "Identifier of the company", example = "11112222-3333-4444-5555-666677778888")
            @PathVariable UUID companyId,
            @Parameter(description = "Identifier of the target member (user id)", example = "3f1c8d2e-9b4a-4d6e-8a1f-2c3b4d5e6f70")
            @PathVariable UUID userId,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return companyService.getMemberPermissions(principal.userId(), companyId, userId);
    }

    @PutMapping("/{companyId}/members/{userId}/permissions")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Set a member's permissions",
            description = "Replaces the full set of permissions granted to a member. Requires the MANAGE_PERMISSIONS permission (or OWNER). "
                    + "The owner's permissions cannot be changed (the owner always has every permission)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Member permissions updated",
                    content = @Content(schema = @Schema(implementation = MemberPermissionsResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed, or attempt to modify the owner",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller lacks the MANAGE_PERMISSIONS permission",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company or member not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public MemberPermissionsResponse setMemberPermissions(
            @Parameter(description = "Identifier of the company", example = "11112222-3333-4444-5555-666677778888")
            @PathVariable UUID companyId,
            @Parameter(description = "Identifier of the target member (user id)", example = "3f1c8d2e-9b4a-4d6e-8a1f-2c3b4d5e6f70")
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateMemberPermissionsRequest request,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return companyService.setMemberPermissions(principal.userId(), companyId, userId, request);
    }
}
