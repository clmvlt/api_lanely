package com.lanely.api.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.lanely.api.dto.auth.DriverGoogleLoginRequest;
import com.lanely.api.dto.auth.GoogleAuthResponse;
import com.lanely.api.dto.auth.GoogleLoginRequest;
import com.lanely.api.dto.auth.GoogleRegisterRequest;
import com.lanely.api.dto.auth.GoogleRegistrationDraft;
import com.lanely.api.dto.auth.ProfileAuthResponse;
import com.lanely.api.dto.auth.TokenResponse;
import com.lanely.api.dto.auth.UserAuthResponse;
import com.lanely.api.entity.Company;
import com.lanely.api.entity.Image;
import com.lanely.api.entity.User;
import com.lanely.api.entity.enums.AuthProvider;
import com.lanely.api.exception.InvalidCredentialsException;
import com.lanely.api.mapper.UserMapper;
import com.lanely.api.repository.CompanyMemberRepository;
import com.lanely.api.repository.CompanyRepository;
import com.lanely.api.repository.UserRepository;
import com.lanely.api.security.SubjectType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Service
public class GoogleAuthService {

    private final GoogleIdTokenVerifier verifier;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final CompanyMemberRepository companyMemberRepository;
    private final SessionService sessionService;
    private final PasswordEncoder passwordEncoder;
    private final ImageService imageService;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    public GoogleAuthService(GoogleIdTokenVerifier verifier, UserRepository userRepository,
                             CompanyRepository companyRepository, CompanyMemberRepository companyMemberRepository,
                             SessionService sessionService, PasswordEncoder passwordEncoder,
                             ImageService imageService) {
        this.verifier = verifier;
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.companyMemberRepository = companyMemberRepository;
        this.sessionService = sessionService;
        this.passwordEncoder = passwordEncoder;
        this.imageService = imageService;
    }

    @Transactional
    public GoogleAuthResponse loginWithGoogle(GoogleLoginRequest request, DeviceMeta deviceMeta) {
        GoogleIdToken.Payload payload = verify(request.idToken());
        requireVerifiedEmail(payload);

        String googleId = payload.getSubject();
        String email = payload.getEmail().trim().toLowerCase();

        Optional<User> existing = findAndLink(googleId, email);
        if (existing.isPresent()) {
            return GoogleAuthResponse.authenticated(authenticate(existing.get(), deviceMeta));
        }

        return GoogleAuthResponse.registrationRequired(new GoogleRegistrationDraft(
                email, resolveFirstName(payload, email), resolveLastName(payload)));
    }

    @Transactional
    public UserAuthResponse registerWithGoogle(GoogleRegisterRequest request, DeviceMeta deviceMeta) {
        GoogleIdToken.Payload payload = verify(request.idToken());
        requireVerifiedEmail(payload);

        String googleId = payload.getSubject();
        String email = payload.getEmail().trim().toLowerCase();
        String pictureUrl = (String) payload.get("picture");

        User user = findAndLink(googleId, email)
                .orElseGet(() -> create(email, googleId, request.firstName().trim(), request.lastName().trim(), pictureUrl));

        return authenticate(user, deviceMeta);
    }

    @Transactional
    public ProfileAuthResponse loginDriverWithGoogle(DriverGoogleLoginRequest request, DeviceMeta deviceMeta) {
        GoogleIdToken.Payload payload = verify(request.idToken());
        requireVerifiedEmail(payload);

        String googleId = payload.getSubject();
        String email = payload.getEmail().trim().toLowerCase();

        Company company = companyRepository.findByPublicCode(request.companyCode().trim())
                .orElseThrow(() -> new InvalidCredentialsException("error.credentials.invalid-profile"));

        User user = findAndLink(googleId, email)
                .orElseThrow(() -> new InvalidCredentialsException("error.credentials.invalid-profile"));
        if (!user.isActive()) {
            throw new InvalidCredentialsException("error.google.account-disabled");
        }
        if (!companyMemberRepository.existsByCompanyIdAndUserId(company.getId(), user.getId())) {
            throw new InvalidCredentialsException("error.driver.not-member");
        }

        TokenResponse tokens = sessionService.create(user, company, deviceMeta);
        return new ProfileAuthResponse(tokens, user.getId(), company.getId(), UserMapper.displayName(user),
                SubjectType.DRIVER.name());
    }

    private Optional<User> findAndLink(String googleId, String email) {
        return userRepository.findByGoogleId(googleId)
                .or(() -> userRepository.findByEmailIgnoreCase(email).map(user -> link(user, googleId)));
    }

    private UserAuthResponse authenticate(User user, DeviceMeta deviceMeta) {
        if (!user.isActive()) {
            throw new InvalidCredentialsException("error.google.account-disabled");
        }
        TokenResponse tokens = sessionService.create(user, deviceMeta);
        return new UserAuthResponse(tokens, UserMapper.toSummary(user));
    }

    private User create(String email, String googleId, String firstName, String lastName, String pictureUrl) {
        User user = new User();
        user.setEmail(email);
        user.setGoogleId(googleId);
        user.setAuthProvider(AuthProvider.GOOGLE);
        user.setEmailVerified(true);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
        fetchGoogleAvatar(pictureUrl).ifPresent(user::setProfileImage);
        return userRepository.save(user);
    }

    private Optional<Image> fetchGoogleAvatar(String pictureUrl) {
        if (pictureUrl == null || pictureUrl.isBlank()) {
            return Optional.empty();
        }
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(pictureUrl))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() != 200) {
                return Optional.empty();
            }
            String contentType = response.headers().firstValue("Content-Type")
                    .map(value -> value.split(";")[0].trim())
                    .orElse(null);
            return imageService.tryStore(response.body(), contentType, "google-avatar");
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        } catch (RuntimeException | IOException ex) {
            return Optional.empty();
        }
    }

    private User link(User user, String googleId) {
        user.setGoogleId(googleId);
        user.setEmailVerified(true);
        return user;
    }

    private void requireVerifiedEmail(GoogleIdToken.Payload payload) {
        if (!Boolean.TRUE.equals(payload.getEmailVerified())) {
            throw new InvalidCredentialsException("error.google.email-not-verified");
        }
    }

    private GoogleIdToken.Payload verify(String rawIdToken) {
        try {
            GoogleIdToken idToken = verifier.verify(rawIdToken);
            if (idToken == null) {
                throw new InvalidCredentialsException("error.google.invalid-token");
            }
            return idToken.getPayload();
        } catch (GeneralSecurityException | IOException ex) {
            throw new InvalidCredentialsException("error.google.verification-failed");
        }
    }

    private String resolveFirstName(GoogleIdToken.Payload payload, String email) {
        String givenName = (String) payload.get("given_name");
        if (givenName != null && !givenName.isBlank()) {
            return givenName.trim();
        }
        String name = (String) payload.get("name");
        if (name != null && !name.isBlank()) {
            return name.trim();
        }
        return email;
    }

    private String resolveLastName(GoogleIdToken.Payload payload) {
        String familyName = (String) payload.get("family_name");
        return familyName != null ? familyName.trim() : "";
    }
}
