package com.lanely.api.service;

import com.lanely.api.config.SiteProperties;
import com.lanely.api.dto.auth.TokenResponse;
import com.lanely.api.dto.auth.UserAuthResponse;
import com.lanely.api.entity.EmailVerificationToken;
import com.lanely.api.entity.User;
import com.lanely.api.exception.BadRequestException;
import com.lanely.api.exception.ResourceNotFoundException;
import com.lanely.api.mapper.UserMapper;
import com.lanely.api.repository.EmailVerificationTokenRepository;
import com.lanely.api.repository.UserRepository;
import com.lanely.api.service.mail.MailService;
import com.lanely.api.service.support.CodeGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class EmailVerificationService {

    private static final long TTL_HOURS = 48;

    private final EmailVerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final MailService mailService;
    private final CodeGenerator codeGenerator;
    private final SessionService sessionService;
    private final String siteUrl;

    public EmailVerificationService(EmailVerificationTokenRepository tokenRepository, UserRepository userRepository,
                                    MailService mailService, CodeGenerator codeGenerator, SessionService sessionService,
                                    SiteProperties siteProperties) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.mailService = mailService;
        this.codeGenerator = codeGenerator;
        this.sessionService = sessionService;
        this.siteUrl = siteProperties.url();
    }

    @Transactional
    public void createAndSend(User user) {
        EmailVerificationToken token = new EmailVerificationToken();
        token.setUser(user);
        token.setToken(generateUniqueToken());
        token.setExpiresAt(Instant.now().plus(TTL_HOURS, ChronoUnit.HOURS));
        tokenRepository.save(token);

        String verifyLink = siteUrl + "/verify-email?token=" + token.getToken();
        mailService.sendEmailVerification(user.getEmail(), verifyLink);
    }

    @Transactional
    public UserAuthResponse verify(String rawToken, DeviceMeta deviceMeta) {
        EmailVerificationToken token = tokenRepository.findByToken(rawToken.trim())
                .orElseThrow(() -> new ResourceNotFoundException("error.verification-token.not-found"));
        // TEMPORAIRE (a retirer sur demande) : on autorise la reverification meme si le token a deja ete utilise.
        // if (token.isUsed()) {
        //     throw new BadRequestException("This verification link has already been used");
        // }
        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException("error.verification.expired");
        }
        User user = token.getUser();
        user.setEmailVerified(true);
        token.setUsedAt(Instant.now());

        TokenResponse tokens = sessionService.create(user, deviceMeta);
        return new UserAuthResponse(tokens, UserMapper.toSummary(user));
    }

    @Transactional
    public void resend(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("error.user.not-found"));
        if (user.isEmailVerified()) {
            throw new BadRequestException("error.email.already-verified");
        }
        createAndSend(user);
    }

    private String generateUniqueToken() {
        String token;
        do {
            token = codeGenerator.secureToken();
        } while (tokenRepository.findByToken(token).isPresent());
        return token;
    }
}
