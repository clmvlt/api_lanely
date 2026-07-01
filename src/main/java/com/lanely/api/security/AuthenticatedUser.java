package com.lanely.api.security;

import java.util.UUID;

public record AuthenticatedUser(UUID userId, String email, boolean emailVerified, UUID sessionId) implements AuthenticatedPrincipal {

    @Override
    public UUID accountId() {
        return userId;
    }
}
