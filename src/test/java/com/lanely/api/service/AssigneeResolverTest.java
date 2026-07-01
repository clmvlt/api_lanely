package com.lanely.api.service;

import com.lanely.api.entity.Account;
import com.lanely.api.entity.Company;
import com.lanely.api.entity.Profile;
import com.lanely.api.entity.User;
import com.lanely.api.exception.TransportAssignmentException;
import com.lanely.api.repository.AccountRepository;
import com.lanely.api.repository.CompanyMemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AssigneeResolverTest {

    @Mock private AccountRepository accountRepository;
    @Mock private CompanyMemberRepository companyMemberRepository;

    private AssigneeResolver resolver;

    private final UUID companyId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        resolver = new AssigneeResolver(accountRepository, companyMemberRepository);
    }

    @Test
    void resolveAssignee_memberUser_isAllowed() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        when(accountRepository.findById(userId)).thenReturn(Optional.of(user));
        when(companyMemberRepository.existsByCompanyIdAndUserId(companyId, userId)).thenReturn(true);

        Account resolved = resolver.resolveAssignee(companyId, userId);

        assertSame(user, resolved);
    }

    @Test
    void resolveAssignee_nonMemberUser_isRejected() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        when(accountRepository.findById(userId)).thenReturn(Optional.of(user));
        when(companyMemberRepository.existsByCompanyIdAndUserId(companyId, userId)).thenReturn(false);

        assertThrows(TransportAssignmentException.class, () -> resolver.resolveAssignee(companyId, userId));
    }

    @Test
    void resolveAssignee_profileOfCompany_isAllowed() {
        UUID profileId = UUID.randomUUID();
        Company company = new Company();
        company.setId(companyId);
        Profile profile = new Profile();
        profile.setId(profileId);
        profile.setCompany(company);
        when(accountRepository.findById(profileId)).thenReturn(Optional.of(profile));

        Account resolved = resolver.resolveAssignee(companyId, profileId);

        assertSame(profile, resolved);
    }

    @Test
    void resolveAssignee_profileOfAnotherCompany_isRejected() {
        UUID profileId = UUID.randomUUID();
        Company otherCompany = new Company();
        otherCompany.setId(UUID.randomUUID());
        Profile profile = new Profile();
        profile.setId(profileId);
        profile.setCompany(otherCompany);
        when(accountRepository.findById(profileId)).thenReturn(Optional.of(profile));

        assertThrows(TransportAssignmentException.class, () -> resolver.resolveAssignee(companyId, profileId));
    }
}
