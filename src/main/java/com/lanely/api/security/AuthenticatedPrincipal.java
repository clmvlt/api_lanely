package com.lanely.api.security;

import java.util.UUID;

public interface AuthenticatedPrincipal {

    UUID accountId();

    UUID sessionId();
}
