package com.lanely.api.service;

import com.lanely.api.config.RefreshProperties;
import com.lanely.api.dto.auth.TokenResponse;
import com.lanely.api.entity.Account;
import com.lanely.api.entity.Company;
import com.lanely.api.entity.Session;
import com.lanely.api.exception.InvalidCredentialsException;
import com.lanely.api.exception.ResourceNotFoundException;
import com.lanely.api.repository.SessionRepository;
import com.lanely.api.security.JwtService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
public class SessionService {

    private static final String BEARER = "Bearer";

    private final SessionRepository sessionRepository;
    private final JwtService jwtService;
    private final long refreshTtlSeconds;
    private final SecureRandom secureRandom = new SecureRandom();

    public SessionService(SessionRepository sessionRepository, JwtService jwtService, RefreshProperties refreshProperties) {
        this.sessionRepository = sessionRepository;
        this.jwtService = jwtService;
        this.refreshTtlSeconds = refreshProperties.ttlDays() * 86400L;
    }

    @Transactional
    public TokenResponse create(Account account, DeviceMeta deviceMeta) {
        return create(account, null, deviceMeta);
    }

    @Transactional
    public TokenResponse create(Account account, Company driverCompany, DeviceMeta deviceMeta) {
        Instant now = Instant.now();
        String rawRefreshToken = generateRefreshToken();

        Session session = new Session();
        session.setAccount(account);
        session.setDriverCompany(driverCompany);
        session.setRefreshTokenHash(hash(rawRefreshToken));
        session.setDeviceLabel(deviceMeta.deviceLabel());
        session.setUserAgent(deviceMeta.userAgent());
        session.setIpAddress(deviceMeta.ipAddress());
        session.setLastUsedAt(now);
        session.setExpiresAt(now.plusSeconds(refreshTtlSeconds));
        sessionRepository.save(session);

        return buildTokens(account, session, rawRefreshToken);
    }

    @Transactional
    public TokenResponse rotate(String rawRefreshToken, DeviceMeta deviceMeta) {
        Session session = sessionRepository.findByRefreshTokenHash(hash(rawRefreshToken))
                .orElseThrow(() -> new InvalidCredentialsException("error.refresh-token.invalid"));

        if (session.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidCredentialsException("error.refresh-token.no-longer-valid");
        }

        String newRefreshToken = generateRefreshToken();
        session.setRefreshTokenHash(hash(newRefreshToken));
        session.setLastUsedAt(Instant.now());
        if (deviceMeta.userAgent() != null) {
            session.setUserAgent(deviceMeta.userAgent());
        }
        if (deviceMeta.ipAddress() != null) {
            session.setIpAddress(deviceMeta.ipAddress());
        }

        return buildTokens(session.getAccount(), session, newRefreshToken);
    }

    @Transactional(readOnly = true)
    public List<Session> listForAccount(UUID accountId) {
        return sessionRepository.findByAccountId(accountId);
    }

    @Transactional
    public void revoke(UUID accountId, UUID sessionId) {
        Session session = sessionRepository.findByIdAndAccountId(sessionId, accountId)
                .orElseThrow(() -> new ResourceNotFoundException("error.session.not-found"));
        sessionRepository.delete(session);
    }

    @Transactional
    public void revokeOthers(UUID accountId, UUID currentSessionId) {
        sessionRepository.deleteByAccountIdAndIdNot(accountId, currentSessionId);
    }

    @Transactional
    public void revokeAllForAccount(UUID accountId) {
        sessionRepository.deleteByAccountId(accountId);
    }

    @Transactional
    public void deleteAllForAccount(UUID accountId) {
        sessionRepository.deleteByAccountId(accountId);
    }

    private TokenResponse buildTokens(Account account, Session session, String rawRefreshToken) {
        String accessToken = jwtService.generateAccessToken(account, session);
        return new TokenResponse(
                accessToken,
                BEARER,
                jwtService.getAccessTtlSeconds(),
                rawRefreshToken,
                refreshTtlSeconds);
    }

    private String generateRefreshToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(hashed.length * 2);
            for (byte b : hashed) {
                builder.append(Character.forDigit((b >> 4) & 0xF, 16));
                builder.append(Character.forDigit(b & 0xF, 16));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algorithm not available", ex);
        }
    }
}
