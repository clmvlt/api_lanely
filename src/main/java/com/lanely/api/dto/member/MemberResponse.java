package com.lanely.api.dto.member;

import com.lanely.api.entity.enums.CompanyRole;
import com.lanely.api.entity.enums.Permission;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;
import java.util.UUID;

@Schema(name = "MemberResponse", description = "A user member of a company together with their role and effective permissions")
public record MemberResponse(

        @Schema(description = "User account identifier", example = "3f1c8d2e-9b4a-4d6e-8a1f-2c3b4d5e6f70")
        UUID userId,

        @Schema(description = "User email", example = "jane.doe@example.com")
        String email,

        @Schema(description = "User first name", example = "Jane")
        String firstName,

        @Schema(description = "User last name", example = "Doe")
        String lastName,

        @Schema(description = "Relative URL of the profile picture, or null if none", example = "/images/aaaa1111-bbbb-2222-cccc-3333dddd4444", nullable = true)
        String profileImageUrl,

        @Schema(description = "Role of the user within the company", example = "MEMBER")
        CompanyRole role,

        @Schema(description = "Effective permissions of the member (an OWNER implicitly has all permissions)", example = "[\"MANAGE_PROFILES\"]")
        Set<Permission> permissions
) {
}
