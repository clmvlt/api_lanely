package com.lanely.api.security;

import com.lanely.api.repository.SessionRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final SessionRepository sessionRepository;

    public JwtAuthenticationFilter(JwtService jwtService, SessionRepository sessionRepository) {
        this.jwtService = jwtService;
        this.sessionRepository = sessionRepository;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith(BEARER_PREFIX)
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            authenticate(header.substring(BEARER_PREFIX.length()));
        }
        filterChain.doFilter(request, response);
    }

    private void authenticate(String token) {
        try {
            Claims claims = jwtService.parse(token);
            String subjectType = claims.get("subjectType", String.class);
            UUID accountId = UUID.fromString(claims.getSubject());
            UUID sessionId = UUID.fromString(claims.get("sid", String.class));

            if (!sessionRepository.existsByIdAndAccountId(sessionId, accountId)) {
                SecurityContextHolder.clearContext();
                return;
            }

            if (SubjectType.USER.name().equals(subjectType)) {
                boolean emailVerified = Boolean.TRUE.equals(claims.get("emailVerified", Boolean.class));
                AuthenticatedUser principal = new AuthenticatedUser(accountId, claims.get("email", String.class), emailVerified, sessionId);
                setAuthentication(principal, "ROLE_USER");
            } else if (SubjectType.PROFILE.name().equals(subjectType)) {
                UUID companyId = UUID.fromString(claims.get("companyId", String.class));
                AuthenticatedProfile principal = new AuthenticatedProfile(accountId, companyId, claims.get("username", String.class), sessionId);
                setAuthentication(principal, "ROLE_PROFILE");
            } else if (SubjectType.DRIVER.name().equals(subjectType)) {
                UUID companyId = UUID.fromString(claims.get("companyId", String.class));
                AuthenticatedProfile principal = new AuthenticatedProfile(accountId, companyId, claims.get("name", String.class), sessionId);
                setAuthentication(principal, "ROLE_PROFILE");
            }
        } catch (JwtException | IllegalArgumentException ex) {
            SecurityContextHolder.clearContext();
        }
    }

    private void setAuthentication(Object principal, String role) {
        var authentication = new UsernamePasswordAuthenticationToken(
                principal, null, List.of(new SimpleGrantedAuthority(role)));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
