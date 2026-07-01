package com.lanely.api.dto.me;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

@Schema(name = "UserMe", description = "Identity of the current web user plus the companies they belong to")
public record UserMe(

        @Schema(description = "User account identifier", example = "3f1c8d2e-9b4a-4d6e-8a1f-2c3b4d5e6f70")
        UUID id,

        @Schema(description = "Email address", example = "jane.doe@example.com")
        String email,

        @Schema(description = "First name", example = "Jane")
        String firstName,

        @Schema(description = "Last name", example = "Doe")
        String lastName,

        @Schema(description = "Whether the email address has been verified", example = "false")
        boolean emailVerified,

        @Schema(description = "Relative URL of the profile picture, or null if none", example = "/images/aaaa1111-bbbb-2222-cccc-3333dddd4444", nullable = true)
        String profileImageUrl,

        @Schema(description = "Technical code of the user's current subscription plan, or null if they have no active subscription. "
                + "Resolve full details (price, limits, label) via GET /subscription-plans, or GET /me/subscription for plan + usage. Read-only.",
                example = "STARTER", nullable = true)
        String subscriptionPlanCode,

        @Schema(description = "Companies the user is a member of")
        List<MembershipSummary> companies
) {
}
