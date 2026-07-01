package com.lanely.api.service;

import com.lanely.api.dto.auth.ProfileAuthResponse;
import com.lanely.api.dto.auth.ProfileLoginRequest;
import com.lanely.api.dto.auth.RefreshRequest;
import com.lanely.api.dto.auth.RegisterUserRequest;
import com.lanely.api.dto.auth.TokenResponse;
import com.lanely.api.dto.auth.UserAuthResponse;
import com.lanely.api.dto.auth.UserLoginRequest;
import com.lanely.api.entity.Company;
import com.lanely.api.entity.Profile;
import com.lanely.api.entity.User;
import com.lanely.api.exception.InvalidCredentialsException;
import com.lanely.api.mapper.UserMapper;
import com.lanely.api.repository.CompanyMemberRepository;
import com.lanely.api.repository.CompanyRepository;
import com.lanely.api.repository.ProfileRepository;
import com.lanely.api.repository.UserRepository;
import com.lanely.api.security.SubjectType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private final UserService userService;
    private final SessionService sessionService;
    private final EmailVerificationService emailVerificationService;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final CompanyRepository companyRepository;
    private final CompanyMemberRepository companyMemberRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserService userService, SessionService sessionService,
                       EmailVerificationService emailVerificationService, UserRepository userRepository,
                       ProfileRepository profileRepository, CompanyRepository companyRepository,
                       CompanyMemberRepository companyMemberRepository, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.sessionService = sessionService;
        this.emailVerificationService = emailVerificationService;
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.companyRepository = companyRepository;
        this.companyMemberRepository = companyMemberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserAuthResponse registerAndLogin(RegisterUserRequest request, DeviceMeta deviceMeta) {
        User user = userService.register(request);
        emailVerificationService.createAndSend(user);
        return toUserAuthResponse(user, deviceMeta);
    }

    @Transactional
    public UserAuthResponse loginUser(UserLoginRequest request, DeviceMeta deviceMeta) {
        User user = userRepository.findByEmailIgnoreCase(request.email().trim().toLowerCase())
                .orElseThrow(() -> new InvalidCredentialsException("error.credentials.invalid"));
        if (!user.isActive() || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("error.credentials.invalid");
        }
        return toUserAuthResponse(user, deviceMeta);
    }

    @Transactional
    public ProfileAuthResponse loginProfile(ProfileLoginRequest request, DeviceMeta deviceMeta) {
        Company company = companyRepository.findByPublicCode(request.companyCode().trim())
                .orElseThrow(() -> new InvalidCredentialsException("error.credentials.invalid-profile"));

        Optional<Profile> profile = profileRepository.findByCompanyIdAndUsername(company.getId(), request.username().trim());
        if (profile.isPresent()) {
            Profile matched = profile.get();
            if (!matched.isActive() || !passwordEncoder.matches(request.password(), matched.getPasswordHash())) {
                throw new InvalidCredentialsException("error.credentials.invalid-profile");
            }
            TokenResponse tokens = sessionService.create(matched, deviceMeta);
            return new ProfileAuthResponse(tokens, matched.getId(), company.getId(), matched.getUsername(),
                    SubjectType.PROFILE.name());
        }

        User user = userRepository.findByEmailIgnoreCase(request.username().trim().toLowerCase())
                .orElseThrow(() -> new InvalidCredentialsException("error.credentials.invalid-profile"));
        if (!user.isActive() || !passwordEncoder.matches(request.password(), user.getPasswordHash())
                || !companyMemberRepository.existsByCompanyIdAndUserId(company.getId(), user.getId())) {
            throw new InvalidCredentialsException("error.credentials.invalid-profile");
        }
        TokenResponse tokens = sessionService.create(user, company, deviceMeta);
        return new ProfileAuthResponse(tokens, user.getId(), company.getId(), UserMapper.displayName(user),
                SubjectType.DRIVER.name());
    }

    @Transactional
    public TokenResponse refresh(RefreshRequest request, DeviceMeta deviceMeta) {
        return sessionService.rotate(request.refreshToken(), deviceMeta);
    }

    @Transactional
    public void logout(UUID accountId, UUID sessionId) {
        sessionService.revoke(accountId, sessionId);
    }

    private UserAuthResponse toUserAuthResponse(User user, DeviceMeta deviceMeta) {
        TokenResponse tokens = sessionService.create(user, deviceMeta);
        return new UserAuthResponse(tokens, UserMapper.toSummary(user));
    }
}
