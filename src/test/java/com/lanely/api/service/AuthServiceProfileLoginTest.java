package com.lanely.api.service;

import com.lanely.api.dto.auth.ProfileAuthResponse;
import com.lanely.api.dto.auth.ProfileLoginRequest;
import com.lanely.api.dto.auth.TokenResponse;
import com.lanely.api.entity.Company;
import com.lanely.api.entity.Profile;
import com.lanely.api.entity.User;
import com.lanely.api.exception.InvalidCredentialsException;
import com.lanely.api.repository.CompanyMemberRepository;
import com.lanely.api.repository.CompanyRepository;
import com.lanely.api.repository.ProfileRepository;
import com.lanely.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceProfileLoginTest {

    @Mock private UserService userService;
    @Mock private SessionService sessionService;
    @Mock private EmailVerificationService emailVerificationService;
    @Mock private UserRepository userRepository;
    @Mock private ProfileRepository profileRepository;
    @Mock private CompanyRepository companyRepository;
    @Mock private CompanyMemberRepository companyMemberRepository;
    @Mock private PasswordEncoder passwordEncoder;

    private AuthService service;

    private final UUID companyId = UUID.randomUUID();
    private final UUID profileId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final DeviceMeta deviceMeta = new DeviceMeta(null, null, null);
    private final TokenResponse tokens = new TokenResponse("access", "Bearer", 900L, "refresh", 2592000L);

    private Company company;

    @BeforeEach
    void setUp() {
        service = new AuthService(userService, sessionService, emailVerificationService, userRepository,
                profileRepository, companyRepository, companyMemberRepository, passwordEncoder);

        company = new Company();
        company.setId(companyId);
        when(companyRepository.findByPublicCode("ACME01")).thenReturn(Optional.of(company));
        when(passwordEncoder.matches(eq("secret"), any())).thenReturn(true);
    }

    private ProfileLoginRequest request(String identifier) {
        return new ProfileLoginRequest("ACME01", identifier, "secret");
    }

    @Test
    void loginProfile_matchingProfile_opensProfileSession() {
        Profile profile = new Profile();
        profile.setId(profileId);
        profile.setActive(true);
        profile.setPasswordHash("profile-hash");
        profile.setCompany(company);
        profile.setUsername("driver01");
        when(profileRepository.findByCompanyIdAndUsername(companyId, "driver01")).thenReturn(Optional.of(profile));
        when(sessionService.create(eq(profile), any(DeviceMeta.class))).thenReturn(tokens);

        ProfileAuthResponse response = service.loginProfile(request("driver01"), deviceMeta);

        assertEquals("PROFILE", response.subjectType());
        assertEquals(profileId, response.profileId());
        assertEquals(companyId, response.companyId());
        assertEquals("driver01", response.username());
        verify(sessionService).create(eq(profile), any(DeviceMeta.class));
        verify(sessionService, never()).create(any(), any(Company.class), any());
    }

    @Test
    void loginProfile_webUserMember_opensDriverSession() {
        when(profileRepository.findByCompanyIdAndUsername(companyId, "boss@acme.com")).thenReturn(Optional.empty());
        User user = memberUser();
        when(userRepository.findByEmailIgnoreCase("boss@acme.com")).thenReturn(Optional.of(user));
        when(companyMemberRepository.existsByCompanyIdAndUserId(companyId, userId)).thenReturn(true);
        when(sessionService.create(eq(user), eq(company), any(DeviceMeta.class))).thenReturn(tokens);

        ProfileAuthResponse response = service.loginProfile(request("boss@acme.com"), deviceMeta);

        assertEquals("DRIVER", response.subjectType());
        assertEquals(userId, response.profileId());
        assertEquals(companyId, response.companyId());
        assertEquals("Jane Doe", response.username());
        verify(sessionService).create(eq(user), eq(company), any(DeviceMeta.class));
    }

    @Test
    void loginProfile_webUserNotMember_isRejected() {
        when(profileRepository.findByCompanyIdAndUsername(companyId, "boss@acme.com")).thenReturn(Optional.empty());
        User user = memberUser();
        when(userRepository.findByEmailIgnoreCase("boss@acme.com")).thenReturn(Optional.of(user));
        when(companyMemberRepository.existsByCompanyIdAndUserId(companyId, userId)).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> service.loginProfile(request("boss@acme.com"), deviceMeta));
        verify(sessionService, never()).create(any(), any(Company.class), any());
    }

    @Test
    void loginProfile_unknownIdentifier_isRejected() {
        when(profileRepository.findByCompanyIdAndUsername(companyId, "ghost")).thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase("ghost")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> service.loginProfile(request("ghost"), deviceMeta));
    }

    private User memberUser() {
        User user = new User();
        user.setId(userId);
        user.setActive(true);
        user.setPasswordHash("user-hash");
        user.setEmail("boss@acme.com");
        user.setFirstName("Jane");
        user.setLastName("Doe");
        return user;
    }
}
