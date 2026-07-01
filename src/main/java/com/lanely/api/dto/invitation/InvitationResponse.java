package com.lanely.api.dto.invitation;

import com.lanely.api.entity.enums.InvitationStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(name = "InvitationResponse", description = "An invitation to join a company. The code and join link are only populated in non-production environments.")
public record InvitationResponse(

        @Schema(description = "Unique invitation identifier", example = "99990000-1111-2222-3333-444455556666")
        UUID id,

        @Schema(description = "Invited email address", example = "new.member@example.com")
        String email,

        @Schema(description = "Current status of the invitation", example = "PENDING")
        InvitationStatus status,

        @Schema(description = "Instant the invitation expires (ISO-8601 UTC)", example = "2026-06-17T09:00:00Z")
        Instant expiresAt,

        @Schema(description = "Join code embedded in the invitation link. Null in production (sent by email instead).", example = "Zm9vYmFyYmF6cXV4MTIzNDU2Nzg", nullable = true)
        String code,

        @Schema(description = "Full join link the invitee can follow. Null in production.", example = "http://localhost:8080/join?code=Zm9vYmFyYmF6cXV4MTIzNDU2Nzg", nullable = true)
        String joinLink
) {
}
