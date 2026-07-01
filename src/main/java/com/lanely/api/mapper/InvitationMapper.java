package com.lanely.api.mapper;

import com.lanely.api.dto.invitation.InvitationResponse;
import com.lanely.api.entity.CompanyInvitation;

public final class InvitationMapper {

    private InvitationMapper() {
    }

    public static InvitationResponse toResponse(CompanyInvitation invitation, String code, String joinLink) {
        return new InvitationResponse(
                invitation.getId(),
                invitation.getEmail(),
                invitation.getStatus(),
                invitation.getExpiresAt(),
                code,
                joinLink);
    }
}
