package com.lanely.api.controller;

import com.lanely.api.controller.support.DeviceMetaResolver;
import com.lanely.api.dto.error.ErrorResponse;
import com.lanely.api.dto.invitation.AcceptInvitationRequest;
import com.lanely.api.dto.invitation.AcceptInvitationResponse;
import com.lanely.api.dto.invitation.InvitationResponse;
import com.lanely.api.dto.invitation.InviteUserRequest;
import com.lanely.api.security.AuthenticatedUser;
import com.lanely.api.service.InvitationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "Invitations", description = "Invite users to a company by email and accept invitations (with optional account creation)")
public class InvitationController {

    private final InvitationService invitationService;

    public InvitationController(InvitationService invitationService) {
        this.invitationService = invitationService;
    }

    @PostMapping("/companies/{companyId}/invitations")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Invite a user to the company",
            description = "Creates a pending invitation for the given email and sends a join link (stubbed: logged, and returned in non-production). "
                    + "Only the company OWNER can invite. Fails if the email is already a member or already has a pending invitation. "
                    + "The invitee can later accept with or without an existing account."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Invitation created",
                    content = @Content(schema = @Schema(implementation = InvitationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed for one or more fields",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller is not the company owner",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Email already a member or already invited",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<InvitationResponse> invite(
            @Parameter(description = "Identifier of the company", example = "11112222-3333-4444-5555-666677778888")
            @PathVariable UUID companyId,
            @Valid @RequestBody InviteUserRequest request,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        InvitationResponse response = invitationService.invite(principal.userId(), companyId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/companies/{companyId}/invitations")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "List company invitations",
            description = "Returns all invitations of the company (any status). Only the company OWNER can list them. "
                    + "Codes and join links are only populated in non-production environments."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Invitations returned",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = InvitationResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller is not the company owner",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public List<InvitationResponse> listInvitations(
            @Parameter(description = "Identifier of the company", example = "11112222-3333-4444-5555-666677778888")
            @PathVariable UUID companyId,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        return invitationService.listInvitations(principal.userId(), companyId);
    }

    @DeleteMapping("/companies/{companyId}/invitations/{invitationId}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Delete a company invitation",
            description = "Permanently deletes an invitation of the company, whatever its status (PENDING, ACCEPTED, EXPIRED or REVOKED). "
                    + "The invitation row is removed from the database and no longer appears in the company's invitation list; if it was "
                    + "still PENDING its join code stops working. Deleting an ACCEPTED invitation does NOT remove the member who already "
                    + "joined. Requires the MANAGE_COMPANY permission (the OWNER always has it). Idempotent from the caller's point of view: "
                    + "a second call on the same invitation returns 404."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Invitation deleted (no content)"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Caller lacks the MANAGE_COMPANY permission",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Company not found, or invitation not found in this company",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteInvitation(
            @Parameter(description = "Identifier of the company", example = "11112222-3333-4444-5555-666677778888")
            @PathVariable UUID companyId,
            @Parameter(description = "Identifier of the invitation to delete", example = "99990000-1111-2222-3333-444455556666")
            @PathVariable UUID invitationId,
            @AuthenticationPrincipal AuthenticatedUser principal) {
        invitationService.deleteInvitation(principal.userId(), companyId, invitationId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/invitations/accept")
    @Operation(
            summary = "Accept an invitation",
            description = "Accepts a pending, non-expired invitation by code and adds the user to the company as a MEMBER. "
                    + "If a Bearer token is supplied, the authenticated user joins (omit 'newAccount'). "
                    + "If called anonymously, 'newAccount' must be provided to create the account on the fly; in that case the response "
                    + "includes fresh tokens and the status is 201. Joining consumes one seat in the company, so acceptance is rejected (403) "
                    + "when the company already reached the seat limit of the owner's subscription plan."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Existing user joined the company",
                    content = @Content(schema = @Schema(implementation = AcceptInvitationResponse.class))),
            @ApiResponse(responseCode = "201", description = "New account created and joined the company",
                    content = @Content(schema = @Schema(implementation = AcceptInvitationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed, or neither authentication nor a new account was provided",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "The company's seat limit is reached",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Invitation code not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Invitation already used/expired, or user already a member",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<AcceptInvitationResponse> accept(
            @Valid @RequestBody AcceptInvitationRequest request,
            @AuthenticationPrincipal AuthenticatedUser principal,
            HttpServletRequest httpRequest) {
        UUID currentUserId = principal != null ? principal.userId() : null;
        AcceptInvitationResponse response = invitationService.accept(currentUserId, request, DeviceMetaResolver.from(httpRequest));
        HttpStatus status = response.auth() != null ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(response);
    }
}
