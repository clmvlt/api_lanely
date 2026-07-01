package com.lanely.api.dto.permission;

import com.lanely.api.entity.enums.Permission;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

@Schema(name = "UpdateMemberPermissionsRequest", description = "Full set of permissions to assign to a member (replaces the previous set)")
public record UpdateMemberPermissionsRequest(

        @Schema(description = "Complete list of permission keys the member should have. An empty list removes all permissions.",
                example = "[\"MANAGE_PROFILES\", \"MANAGE_COMPANY\"]", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        Set<Permission> permissions
) {
}
