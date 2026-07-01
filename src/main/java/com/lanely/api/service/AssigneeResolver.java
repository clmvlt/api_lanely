package com.lanely.api.service;

import com.lanely.api.entity.Account;
import com.lanely.api.entity.Profile;
import com.lanely.api.entity.User;
import com.lanely.api.exception.ResourceNotFoundException;
import com.lanely.api.exception.TransportAssignmentException;
import com.lanely.api.repository.AccountRepository;
import com.lanely.api.repository.CompanyMemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AssigneeResolver {

    private final AccountRepository accountRepository;
    private final CompanyMemberRepository companyMemberRepository;

    public AssigneeResolver(AccountRepository accountRepository, CompanyMemberRepository companyMemberRepository) {
        this.accountRepository = accountRepository;
        this.companyMemberRepository = companyMemberRepository;
    }

    @Transactional(readOnly = true)
    public Account resolveAssignee(UUID companyId, UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("error.account.not-found"));
        if (account instanceof Profile profile) {
            if (!profile.getCompany().getId().equals(companyId)) {
                throw new TransportAssignmentException("error.transport.profile-other-company");
            }
            return profile;
        }
        if (account instanceof User user) {
            if (!companyMemberRepository.existsByCompanyIdAndUserId(companyId, user.getId())) {
                throw new TransportAssignmentException("error.transport.assignee-not-member");
            }
            return user;
        }
        throw new TransportAssignmentException("error.transport.assignee-invalid");
    }
}
