package com.lanely.api.security;

import java.util.UUID;

public record AuthenticatedProfile(UUID profileId, UUID companyId, String username, UUID sessionId) implements AuthenticatedPrincipal {

    @Override
    public UUID accountId() {
        return profileId;
    }
}
