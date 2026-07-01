package com.lanely.api.dto.me;

import com.lanely.api.entity.enums.CompanyRole;
import com.lanely.api.entity.enums.Permission;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;
import java.util.UUID;

@Schema(name = "MembershipSummary", description = "A company the current user belongs to, with their role and effective permissions")
public record MembershipSummary(

        @Schema(description = "Company identifier", example = "11112222-3333-4444-5555-666677778888")
        UUID companyId,

        @Schema(description = "Company name", example = "Speedy Delivery")
        String companyName,

        @Schema(description = "Public company code", example = "K7P2M9QX")
        String publicCode,

        @Schema(description = "Relative URL of the company profile picture, or null if none", example = "/images/aaaa1111-bbbb-2222-cccc-3333dddd4444", nullable = true)
        String profileImageUrl,

        @Schema(description = "Role of the user in this company", example = "OWNER")
        CompanyRole role,

        @Schema(description = "Effective permissions of the user in this company (an OWNER has all of them)", example = "[\"MANAGE_PROFILES\"]")
        Set<Permission> permissions
) {
}
