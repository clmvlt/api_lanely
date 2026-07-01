package com.lanely.api.dto.permission;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "PermissionDto", description = "A permission that can be granted to a company member")
public record PermissionDto(

        @Schema(description = "Stable permission key", example = "MANAGE_PROFILES")
        String key,

        @Schema(description = "Human-readable description of what the permission grants", example = "Create, update, delete, activate and deactivate delivery profiles")
        String description
) {
}
