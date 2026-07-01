package com.lanely.api.dto.company;

import com.lanely.api.entity.enums.CompanyRole;
import com.lanely.api.entity.enums.Permission;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;
import java.util.UUID;

@Schema(name = "CompanyMeResponse", description = "The calling user's role and effective permissions within a company")
public record CompanyMeResponse(

        @Schema(description = "Company identifier", example = "11112222-3333-4444-5555-666677778888")
        UUID companyId,

        @Schema(description = "Role of the caller", example = "OWNER")
        CompanyRole role,

        @Schema(description = "Effective permissions of the caller (an OWNER has all of them)", example = "[\"MANAGE_COMPANY\",\"MANAGE_PROFILES\",\"MANAGE_PERMISSIONS\"]")
        Set<Permission> permissions
) {
}
