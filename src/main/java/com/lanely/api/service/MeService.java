package com.lanely.api.service;

import com.lanely.api.dto.me.MeResponse;
import com.lanely.api.dto.me.MembershipSummary;
import com.lanely.api.dto.me.ProfileMe;
import com.lanely.api.dto.me.UserMe;
import com.lanely.api.entity.Company;
import com.lanely.api.entity.CompanyMember;
import com.lanely.api.entity.Profile;
import com.lanely.api.entity.User;
import com.lanely.api.exception.ResourceNotFoundException;
import com.lanely.api.mapper.ImageMapper;
import com.lanely.api.mapper.MemberMapper;
import com.lanely.api.mapper.UserMapper;
import com.lanely.api.repository.CompanyMemberRepository;
import com.lanely.api.repository.ProfileRepository;
import com.lanely.api.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class MeService {

    private final UserRepository userRepository;
    private final CompanyMemberRepository companyMemberRepository;
    private final ProfileRepository profileRepository;

    public MeService(UserRepository userRepository, CompanyMemberRepository companyMemberRepository,
                     ProfileRepository profileRepository) {
        this.userRepository = userRepository;
        this.companyMemberRepository = companyMemberRepository;
        this.profileRepository = profileRepository;
    }

    @Transactional(readOnly = true)
    public MeResponse forUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("error.user.not-found"));

        List<MembershipSummary> companies = companyMemberRepository.findByUserId(userId).stream()
                .map(member -> {
                    Company company = member.getCompany();
                    return new MembershipSummary(company.getId(), company.getName(), company.getPublicCode(),
                            ImageMapper.url(company.getProfileImage()), member.getRole(), MemberMapper.effectivePermissions(member));
                })
                .toList();

        return MeResponse.ofUser(new UserMe(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(),
                user.isEmailVerified(), ImageMapper.url(user.getProfileImage()), UserMapper.subscriptionPlanCode(user), companies));
    }

    @Transactional(readOnly = true)
    public MeResponse forProfile(UUID profileId) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("error.profile.not-found"));
        Company company = profile.getCompany();
        return MeResponse.ofProfile(new ProfileMe(profile.getId(), profile.getUsername(), profile.getDisplayName(),
                profile.isActive(), company.getId(), company.getName()));
    }

    @Transactional(readOnly = true)
    public MeResponse forDriver(UUID userId, UUID companyId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("error.user.not-found"));
        Company company = companyMemberRepository.findByCompanyIdAndUserId(companyId, userId)
                .map(CompanyMember::getCompany)
                .orElseThrow(() -> new ResourceNotFoundException("error.driver.not-member"));
        String displayName = UserMapper.displayName(user);
        return MeResponse.ofProfile(new ProfileMe(user.getId(), displayName, displayName,
                user.isActive(), company.getId(), company.getName()));
    }
}
