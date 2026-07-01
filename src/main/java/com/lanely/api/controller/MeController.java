package com.lanely.api.controller;

import com.lanely.api.dto.error.ErrorResponse;
import com.lanely.api.dto.me.ChangePasswordRequest;
import com.lanely.api.dto.me.MeResponse;
import com.lanely.api.dto.me.UpdateMeRequest;
import com.lanely.api.exception.BadRequestException;
import com.lanely.api.exception.ResourceNotFoundException;
import com.lanely.api.security.AuthenticatedPrincipal;
import com.lanely.api.security.AuthenticatedProfile;
import com.lanely.api.security.AuthenticatedUser;
import com.lanely.api.service.AccountService;
import com.lanely.api.service.MeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/me")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Me", description = "Self-service for the currently authenticated account: read identity, update own info, change password")
public class MeController {

    private final MeService meService;
    private final AccountService accountService;

    public MeController(MeService meService, AccountService accountService) {
        this.meService = meService;
        this.accountService = accountService;
    }

    @GetMapping
    @Operation(
            summary = "Get the current account",
            description = "Returns the account bound to the access token used for this request. For a web user, returns the user identity "
                    + "and the companies they belong to (with role and permissions). For a mobile profile, returns the profile identity and "
                    + "its company. Typical use: the client stores the token and calls this endpoint on page reload to restore the session. "
                    + "If the access token has expired, this returns 401 - refresh it via /auth/refresh and retry."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Current account returned",
                    content = @Content(schema = @Schema(implementation = MeResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid/expired access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public MeResponse me(@AuthenticationPrincipal Object principal) {
        return resolve(principal);
    }

    @PatchMapping
    @Operation(
            summary = "Update my own information",
            description = "Updates the current web user's own first name and/or last name (only non-null fields are applied). "
                    + "Available for web user accounts; mobile profiles do not carry these fields."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Information updated; returns the refreshed account",
                    content = @Content(schema = @Schema(implementation = MeResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed, or the account type cannot be updated this way",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public MeResponse updateMe(@Valid @RequestBody UpdateMeRequest request,
                               @AuthenticationPrincipal Object principal) {
        if (principal instanceof AuthenticatedUser user) {
            accountService.updateUserInfo(user.userId(), request);
            return meService.forUser(user.userId());
        }
        throw new BadRequestException("error.me.web-only-update");
    }

    @PutMapping("/password")
    @Operation(
            summary = "Change my password",
            description = "Changes the current account's password after verifying the current one. Works for both web users and mobile "
                    + "profiles. For security, all other sessions of this account are revoked; the session used for this request stays active."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Password changed"),
            @ApiResponse(responseCode = "400", description = "Validation failed or current password is incorrect",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                               @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        accountService.changePassword(principal.accountId(), principal.sessionId(), request);
        return ResponseEntity.noContent().build();
    }

    @PutMapping(value = "/picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Set my profile picture",
            description = "Uploads (or replaces) the current web user's profile picture via multipart/form-data (field 'file'). "
                    + "Accepted types: PNG, JPEG, WEBP, GIF. Returns the refreshed account; the picture is then served at the URL in "
                    + "'profileImageUrl'. Any previous picture is deleted."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Picture set; returns the refreshed account",
                    content = @Content(schema = @Schema(implementation = MeResponse.class))),
            @ApiResponse(responseCode = "400", description = "Missing/invalid/too large file, or unsupported account type",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public MeResponse setPicture(@RequestParam("file") MultipartFile file,
                                 @AuthenticationPrincipal Object principal) {
        if (principal instanceof AuthenticatedUser user) {
            accountService.setUserPicture(user.userId(), file);
            return meService.forUser(user.userId());
        }
        throw new BadRequestException("error.me.web-only-picture-set");
    }

    @DeleteMapping("/picture")
    @Operation(
            summary = "Remove my profile picture",
            description = "Removes the current web user's profile picture (and deletes the stored image). No-op if there is none."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Picture removed"),
            @ApiResponse(responseCode = "400", description = "Unsupported account type",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> removePicture(@AuthenticationPrincipal Object principal) {
        if (principal instanceof AuthenticatedUser user) {
            accountService.removeUserPicture(user.userId());
            return ResponseEntity.noContent().build();
        }
        throw new BadRequestException("error.me.web-only-picture-remove");
    }

    private MeResponse resolve(Object principal) {
        if (principal instanceof AuthenticatedUser user) {
            return meService.forUser(user.userId());
        }
        if (principal instanceof AuthenticatedProfile profile) {
            return profile.driver()
                    ? meService.forDriver(profile.profileId(), profile.companyId())
                    : meService.forProfile(profile.profileId());
        }
        throw new ResourceNotFoundException("error.me.no-authenticated-account");
    }
}
