package com.lanely.api.security;

import com.lanely.api.config.JwtProperties;
import com.lanely.api.entity.Account;
import com.lanely.api.entity.Company;
import com.lanely.api.entity.Profile;
import com.lanely.api.entity.Session;
import com.lanely.api.entity.User;
import com.lanely.api.mapper.UserMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey key;
    private final long accessTtlSeconds;

    public JwtService(JwtProperties properties) {
        this.key = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
        this.accessTtlSeconds = properties.accessTtlSeconds();
    }

    public long getAccessTtlSeconds() {
        return accessTtlSeconds;
    }

    public String generateAccessToken(Account account, Session session) {
        Account resolved = (Account) Hibernate.unproxy(account);
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(accessTtlSeconds);

        var builder = Jwts.builder()
                .subject(resolved.getId().toString())
                .claim("sid", session.getId().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry));

        if (resolved instanceof User user) {
            Company driverCompany = session.getDriverCompany();
            if (driverCompany != null) {
                builder.claim("subjectType", SubjectType.DRIVER.name());
                builder.claim("companyId", driverCompany.getId().toString());
                builder.claim("name", UserMapper.displayName(user));
            } else {
                builder.claim("subjectType", SubjectType.USER.name());
                builder.claim("email", user.getEmail());
                builder.claim("emailVerified", user.isEmailVerified());
            }
        } else if (resolved instanceof Profile profile) {
            builder.claim("subjectType", SubjectType.PROFILE.name());
            builder.claim("companyId", profile.getCompany().getId().toString());
            builder.claim("username", profile.getUsername());
        }

        return builder.signWith(key).compact();
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
