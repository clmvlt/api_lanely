package com.lanely.api.dto.invitation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "InviteUserRequest", description = "Payload to invite a user (by email) to join a company as a member")
public record InviteUserRequest(

        @Schema(description = "Email address of the person to invite (case-insensitive). They may or may not already have an account.", example = "new.member@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        @Email
        String email
) {
}
