package com.lanely.api.service;

import com.lanely.api.config.SiteProperties;
import com.lanely.api.entity.PasswordResetToken;
import com.lanely.api.entity.User;
import com.lanely.api.exception.BadRequestException;
import com.lanely.api.exception.ResourceNotFoundException;
import com.lanely.api.repository.PasswordResetTokenRepository;
import com.lanely.api.repository.UserRepository;
import com.lanely.api.service.mail.MailService;
import com.lanely.api.service.support.CodeGenerator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class PasswordResetService {

    private static final long TTL_MINUTES = 60;

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final MailService mailService;
    private final CodeGenerator codeGenerator;
    private final PasswordEncoder passwordEncoder;
    private final SessionService sessionService;
    private final String siteUrl;

    public PasswordResetService(PasswordResetTokenRepository tokenRepository, UserRepository userRepository,
                                MailService mailService, CodeGenerator codeGenerator, PasswordEncoder passwordEncoder,
                                SessionService sessionService, SiteProperties siteProperties) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.mailService = mailService;
        this.codeGenerator = codeGenerator;
        this.passwordEncoder = passwordEncoder;
        this.sessionService = sessionService;
        this.siteUrl = siteProperties.url();
    }

    @Transactional
    public void requestReset(String email) {
        userRepository.findByEmailIgnoreCase(email.trim().toLowerCase())
                .filter(User::isActive)
                .ifPresent(this::createAndSend);
    }

    @Transactional
    public void reset(String rawToken, String newPassword) {
        PasswordResetToken token = tokenRepository.findByToken(rawToken.trim())
                .orElseThrow(() -> new ResourceNotFoundException("error.reset-token.not-found"));
        if (token.isUsed()) {
            throw new BadRequestException("error.reset-token.used");
        }
        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException("error.reset-token.expired");
        }

        User user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setEmailVerified(true);
        token.setUsedAt(Instant.now());

        sessionService.revokeAllForAccount(user.getId());
    }

    private void createAndSend(User user) {
        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setToken(generateUniqueToken());
        token.setExpiresAt(Instant.now().plus(TTL_MINUTES, ChronoUnit.MINUTES));
        tokenRepository.save(token);

        String resetLink = siteUrl + "/reset-password?token=" + token.getToken();
        mailService.sendPasswordReset(user.getEmail(), resetLink);
    }

    private String generateUniqueToken() {
        String token;
        do {
            token = codeGenerator.secureToken();
        } while (tokenRepository.findByToken(token).isPresent());
        return token;
    }
}
