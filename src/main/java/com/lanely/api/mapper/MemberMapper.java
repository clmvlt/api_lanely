package com.lanely.api.mapper;

import com.lanely.api.dto.member.MemberResponse;
import com.lanely.api.entity.CompanyMember;
import com.lanely.api.entity.User;
import com.lanely.api.entity.enums.CompanyRole;
import com.lanely.api.entity.enums.Permission;

import java.util.EnumSet;
import java.util.Set;

public final class MemberMapper {

    private MemberMapper() {
    }

    public static MemberResponse toResponse(CompanyMember member) {
        User user = member.getUser();
        return new MemberResponse(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(),
                ImageMapper.url(user.getProfileImage()), member.getRole(), effectivePermissions(member));
    }

    public static Set<Permission> effectivePermissions(CompanyMember member) {
        if (member.getRole() == CompanyRole.OWNER) {
            return EnumSet.allOf(Permission.class);
        }
        return EnumSet.copyOf(member.getPermissions().isEmpty()
                ? EnumSet.noneOf(Permission.class)
                : member.getPermissions());
    }
}
