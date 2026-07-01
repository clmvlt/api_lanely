package com.lanely.api.controller;

import com.lanely.api.dto.error.ErrorResponse;
import com.lanely.api.dto.session.SessionResponse;
import com.lanely.api.mapper.SessionMapper;
import com.lanely.api.security.AuthenticatedPrincipal;
import com.lanely.api.service.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/auth/sessions")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Sessions", description = "List and revoke the login sessions (devices) of the authenticated account")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping
    @Operation(
            summary = "List my sessions",
            description = "Returns every active session of the authenticated account, one per device/login. Revoked sessions are deleted, "
                    + "so they no longer appear here. The session used for the current request is flagged with current=true. "
                    + "Works for both users and profiles."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sessions returned",
                    content = @Content(array = @io.swagger.v3.oas.annotations.media.ArraySchema(schema = @Schema(implementation = SessionResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public List<SessionResponse> listSessions(@AuthenticationPrincipal AuthenticatedPrincipal principal) {
        UUID currentSessionId = principal.sessionId();
        return sessionService.listForAccount(principal.accountId()).stream()
                .map(session -> SessionMapper.toResponse(session, currentSessionId))
                .toList();
    }

    @DeleteMapping("/{sessionId}")
    @Operation(
            summary = "Revoke (delete) a session",
            description = "Permanently deletes one of the authenticated account's sessions by id (for example to disconnect a lost device). "
                    + "The session row is removed: it disappears from the session list, its refresh token stops working, and its access "
                    + "token is rejected on the very next request (the JWT filter checks the session still exists). Calling this again on "
                    + "the same id returns 404."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Session deleted"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Session not found for this account",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> revokeSession(
            @Parameter(description = "Identifier of the session to revoke", example = "aaaa1111-bbbb-2222-cccc-3333dddd4444")
            @PathVariable UUID sessionId,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        sessionService.revoke(principal.accountId(), sessionId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/revoke-others")
    @Operation(
            summary = "Revoke all other sessions",
            description = "Revokes every session of the authenticated account except the one used for the current request. "
                    + "Useful to disconnect all other devices in a single call."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Other sessions revoked"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> revokeOthers(@AuthenticationPrincipal AuthenticatedPrincipal principal) {
        sessionService.revokeOthers(principal.accountId(), principal.sessionId());
        return ResponseEntity.noContent().build();
    }
}
