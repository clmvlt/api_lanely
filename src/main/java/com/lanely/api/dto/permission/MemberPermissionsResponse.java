package com.lanely.api.dto.permission;

import com.lanely.api.entity.enums.CompanyRole;
import com.lanely.api.entity.enums.Permission;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;
import java.util.UUID;

@Schema(name = "MemberPermissionsResponse", description = "Effective permissions of a member (an OWNER implicitly has all permissions)")
public record MemberPermissionsResponse(

        @Schema(description = "User account identifier", example = "3f1c8d2e-9b4a-4d6e-8a1f-2c3b4d5e6f70")
        UUID userId,

        @Schema(description = "Role of the member in the company", example = "MEMBER")
        CompanyRole role,

        @Schema(description = "Effective permissions", example = "[\"MANAGE_PROFILES\"]")
        Set<Permission> permissions
) {
}
