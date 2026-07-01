package com.lanely.api.mapper;

import com.lanely.api.dto.profile.ProfileResponse;
import com.lanely.api.entity.Profile;

public final class ProfileMapper {

    private ProfileMapper() {
    }

    public static ProfileResponse toResponse(Profile profile) {
        return new ProfileResponse(profile.getId(), profile.getUsername(), profile.getDisplayName(), profile.isActive());
    }
}
