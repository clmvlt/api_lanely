package com.lanely.api.service;

import com.lanely.api.dto.profile.CreateProfileRequest;
import com.lanely.api.dto.profile.ProfileResponse;
import com.lanely.api.dto.profile.UpdateProfileRequest;
import com.lanely.api.entity.Company;
import com.lanely.api.entity.CompanyMember;
import com.lanely.api.entity.Profile;
import com.lanely.api.entity.enums.Permission;
import com.lanely.api.exception.ProfileUsernameTakenException;
import com.lanely.api.exception.ResourceNotFoundException;
import com.lanely.api.mapper.ProfileMapper;
import com.lanely.api.repository.ProfileRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ProfileService {

    private final CompanyService companyService;
    private final SubscriptionService subscriptionService;
    private final SessionService sessionService;
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;

    public ProfileService(CompanyService companyService, SubscriptionService subscriptionService,
                          SessionService sessionService, ProfileRepository profileRepository,
                          PasswordEncoder passwordEncoder) {
        this.companyService = companyService;
        this.subscriptionService = subscriptionService;
        this.sessionService = sessionService;
        this.profileRepository = profileRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public ProfileResponse createProfile(UUID currentUserId, UUID companyId, CreateProfileRequest request) {
        CompanyMember membership = companyService.requirePermission(companyId, currentUserId, Permission.MANAGE_PROFILES);
        Company company = membership.getCompany();
        String username = request.username().trim();

        if (profileRepository.existsByCompanyIdAndUsername(companyId, username)) {
            throw new ProfileUsernameTakenException("error.profile.username-taken");
        }

        subscriptionService.assertCanAddSeat(company);

        Profile profile = new Profile();
        profile.setCompany(company);
        profile.setUsername(username);
        profile.setDisplayName(request.displayName() == null ? null : request.displayName().trim());
        profile.setPasswordHash(passwordEncoder.encode(request.password()));
        profileRepository.save(profile);

        return ProfileMapper.toResponse(profile);
    }

    @Transactional(readOnly = true)
    public List<ProfileResponse> listProfiles(UUID currentUserId, UUID companyId) {
        companyService.requireMember(companyId, currentUserId);
        return profileRepository.findByCompanyId(companyId).stream()
                .map(ProfileMapper::toResponse)
                .toList();
    }

    @Transactional
    public ProfileResponse updateProfile(UUID currentUserId, UUID companyId, UUID profileId, UpdateProfileRequest request) {
        companyService.requirePermission(companyId, currentUserId, Permission.MANAGE_PROFILES);
        Profile profile = loadProfileInCompany(companyId, profileId);

        if (request.username() != null) {
            String username = request.username().trim();
            if (!username.equals(profile.getUsername())
                    && profileRepository.existsByCompanyIdAndUsername(companyId, username)) {
                throw new ProfileUsernameTakenException("error.profile.username-taken");
            }
            profile.setUsername(username);
        }
        if (request.displayName() != null) {
            profile.setDisplayName(request.displayName().trim());
        }
        if (request.password() != null) {
            profile.setPasswordHash(passwordEncoder.encode(request.password()));
        }
        return ProfileMapper.toResponse(profile);
    }

    @Transactional
    public ProfileResponse setActive(UUID currentUserId, UUID companyId, UUID profileId, boolean active) {
        companyService.requirePermission(companyId, currentUserId, Permission.MANAGE_PROFILES);
        Profile profile = loadProfileInCompany(companyId, profileId);
        if (active && !profile.isActive()) {
            subscriptionService.assertCanAddSeat(profile.getCompany());
        }
        profile.setActive(active);
        if (!active) {
            sessionService.revokeAllForAccount(profileId);
        }
        return ProfileMapper.toResponse(profile);
    }

    @Transactional
    public void deleteProfile(UUID currentUserId, UUID companyId, UUID profileId) {
        companyService.requirePermission(companyId, currentUserId, Permission.MANAGE_PROFILES);
        Profile profile = loadProfileInCompany(companyId, profileId);
        sessionService.deleteAllForAccount(profileId);
        profileRepository.delete(profile);
    }

    private Profile loadProfileInCompany(UUID companyId, UUID profileId) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("error.profile.not-found"));
        if (!profile.getCompany().getId().equals(companyId)) {
            throw new ResourceNotFoundException("error.profile.not-found");
        }
        return profile;
    }
}
