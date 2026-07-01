package com.lanely.api.mapper;

import com.lanely.api.dto.session.SessionResponse;
import com.lanely.api.entity.Session;

import java.util.UUID;

public final class SessionMapper {

    private SessionMapper() {
    }

    public static SessionResponse toResponse(Session session, UUID currentSessionId) {
        return new SessionResponse(
                session.getId(),
                session.getDeviceLabel(),
                session.getUserAgent(),
                session.getIpAddress(),
                session.getCreatedAt(),
                session.getLastUsedAt(),
                session.getExpiresAt(),
                session.getId().equals(currentSessionId));
    }
}
