package com.lanely.api.controller;

import com.lanely.api.dto.error.ErrorResponse;
import com.lanely.api.dto.profile.CreateProfileRequest;
import com.lanely.api.dto.profile.ProfileResponse;
import com.lanely.api.dto.profile.UpdateProfileRequest;
import com.lanely.api.security.AuthenticatedUser;
import com.lanely.api.service.ProfileService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/companies/{companyId}/profiles")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Profiles", description = "Manage a company's mobile delivery profiles (simple username/password identities for the mobile app)")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @PostMapping
    @Operation(
            summary = "Create a delivery profile",
            description = "Creates a mobile delivery profile (username + password, no email) within the company. Requires the MANAGE_PROFILES "
                    + "permission (the OWNER always has it). The username must be unique within the company, but the same username may be reused "
                    + "in other companies. A new profile consumes one seat: creation is rejected (403) when the company already reached the seat "
                    + "limit of the owner's subscription plan (active profiles + members). Deactivate or remove someone, or upgrade the plan, to free a seat."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Profile created",
                    content = @Content(schema = @Schema(implementation = ProfileResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed for one or more fields",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller lacks the MANAGE_PROFILES permission, or the company's seat limit is reached",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Username already used in this company",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ProfileResponse> createProfile(
            @Parameter(description = "Identifier of the company", example = "11112222-3333-4444-5555-666677778888")
            @PathVariable UUID companyId,
            @Valid @RequestBody CreateProfileRequest request,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        ProfileResponse response = profileService.createProfile(principal.userId(), companyId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(
            summary = "List delivery profiles",
            description = "Returns the company's mobile delivery profiles. The caller must be a member of the company."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profiles returned",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ProfileResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller is not a member of this company",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public List<ProfileResponse> listProfiles(
            @Parameter(description = "Identifier of the company", example = "11112222-3333-4444-5555-666677778888")
            @PathVariable UUID companyId,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return profileService.listProfiles(principal.userId(), companyId);
    }

    @PatchMapping("/{profileId}")
    @Operation(
            summary = "Update a delivery profile",
            description = "Updates a profile's username, display name and/or password. Only non-null fields are applied. "
                    + "Requires the MANAGE_PROFILES permission. A changed username must remain unique within the company."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile updated",
                    content = @Content(schema = @Schema(implementation = ProfileResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed for one or more fields",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller lacks the MANAGE_PROFILES permission",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company or profile not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Username already used in this company",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ProfileResponse updateProfile(
            @Parameter(description = "Identifier of the company", example = "11112222-3333-4444-5555-666677778888")
            @PathVariable UUID companyId,
            @Parameter(description = "Identifier of the profile", example = "8a1f2c3b-4d5e-6f70-3f1c-8d2e9b4a4d6e")
            @PathVariable UUID profileId,
            @Valid @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return profileService.updateProfile(principal.userId(), companyId, profileId, request);
    }

    @PostMapping("/{profileId}/deactivate")
    @Operation(
            summary = "Deactivate a delivery profile",
            description = "Deactivates a profile: it can no longer log in on the mobile app, and all its existing sessions are revoked "
                    + "(its refresh tokens stop working immediately; any short-lived access token expires shortly after). "
                    + "Requires the MANAGE_PROFILES permission."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile deactivated",
                    content = @Content(schema = @Schema(implementation = ProfileResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller lacks the MANAGE_PROFILES permission",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company or profile not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ProfileResponse deactivateProfile(
            @Parameter(description = "Identifier of the company", example = "11112222-3333-4444-5555-666677778888")
            @PathVariable UUID companyId,
            @Parameter(description = "Identifier of the profile", example = "8a1f2c3b-4d5e-6f70-3f1c-8d2e9b4a4d6e")
            @PathVariable UUID profileId,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return profileService.setActive(principal.userId(), companyId, profileId, false);
    }

    @PostMapping("/{profileId}/activate")
    @Operation(
            summary = "Reactivate a delivery profile",
            description = "Reactivates a previously deactivated profile so it can log in again. Requires the MANAGE_PROFILES permission. "
                    + "Reactivation consumes a seat again, so it is rejected (403) when the company already reached the seat limit of the "
                    + "owner's subscription plan."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile reactivated",
                    content = @Content(schema = @Schema(implementation = ProfileResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller lacks the MANAGE_PROFILES permission, or the company's seat limit is reached",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company or profile not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ProfileResponse activateProfile(
            @Parameter(description = "Identifier of the company", example = "11112222-3333-4444-5555-666677778888")
            @PathVariable UUID companyId,
            @Parameter(description = "Identifier of the profile", example = "8a1f2c3b-4d5e-6f70-3f1c-8d2e9b4a4d6e")
            @PathVariable UUID profileId,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return profileService.setActive(principal.userId(), companyId, profileId, true);
    }

    @DeleteMapping("/{profileId}")
    @Operation(
            summary = "Delete a delivery profile",
            description = "Permanently deletes a profile and all its sessions. Requires the MANAGE_PROFILES permission."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Profile deleted"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller lacks the MANAGE_PROFILES permission",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company or profile not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteProfile(
            @Parameter(description = "Identifier of the company", example = "11112222-3333-4444-5555-666677778888")
            @PathVariable UUID companyId,
            @Parameter(description = "Identifier of the profile", example = "8a1f2c3b-4d5e-6f70-3f1c-8d2e9b4a4d6e")
            @PathVariable UUID profileId,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        profileService.deleteProfile(principal.userId(), companyId, profileId);
        return ResponseEntity.noContent().build();
    }
}
