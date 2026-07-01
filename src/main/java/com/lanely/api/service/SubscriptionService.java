package com.lanely.api.service;

import com.lanely.api.dto.subscription.ChangeSubscriptionRequest;
import com.lanely.api.dto.subscription.ChangeSubscriptionResponse;
import com.lanely.api.dto.subscription.CompanySeatUsage;
import com.lanely.api.dto.subscription.MySubscriptionResponse;
import com.lanely.api.dto.subscription.SubscriptionChangeStatus;
import com.lanely.api.dto.subscription.SubscriptionPlanResponse;
import com.lanely.api.entity.Company;
import com.lanely.api.entity.SubscriptionPlan;
import com.lanely.api.entity.User;
import com.lanely.api.entity.enums.SubscriptionPlanCode;
import com.lanely.api.exception.ResourceNotFoundException;
import com.lanely.api.exception.SubscriptionLimitException;
import com.lanely.api.mapper.SubscriptionPlanMapper;
import com.lanely.api.repository.CompanyMemberRepository;
import com.lanely.api.repository.CompanyRepository;
import com.lanely.api.repository.ProfileRepository;
import com.lanely.api.repository.SubscriptionPlanRepository;
import com.lanely.api.repository.UserRepository;
import com.lanely.api.service.payment.SubscriptionChangeOutcome;
import com.lanely.api.service.payment.SubscriptionGateway;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class SubscriptionService {

    private final SubscriptionPlanRepository planRepository;
    private final CompanyRepository companyRepository;
    private final ProfileRepository profileRepository;
    private final CompanyMemberRepository companyMemberRepository;
    private final UserRepository userRepository;
    private final SubscriptionPlanMapper planMapper;
    private final SubscriptionGateway subscriptionGateway;

    public SubscriptionService(SubscriptionPlanRepository planRepository, CompanyRepository companyRepository,
                               ProfileRepository profileRepository, CompanyMemberRepository companyMemberRepository,
                               UserRepository userRepository, SubscriptionPlanMapper planMapper,
                               SubscriptionGateway subscriptionGateway) {
        this.planRepository = planRepository;
        this.companyRepository = companyRepository;
        this.profileRepository = profileRepository;
        this.companyMemberRepository = companyMemberRepository;
        this.userRepository = userRepository;
        this.planMapper = planMapper;
        this.subscriptionGateway = subscriptionGateway;
    }

    @Transactional(readOnly = true)
    public List<SubscriptionPlanResponse> listPlans() {
        return planRepository.findAllByOrderBySortOrderAsc().stream()
                .map(planMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public MySubscriptionResponse getMySubscription(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("error.user.not-found"));
        return buildMySubscription(user);
    }

    @Transactional
    public ChangeSubscriptionResponse changePlan(UUID userId, ChangeSubscriptionRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("error.user.not-found"));
        SubscriptionPlan target = loadPlan(request.planCode());

        assertDowngradeFits(user, target);

        SubscriptionChangeOutcome outcome = subscriptionGateway.requestPlanChange(user, target);
        SubscriptionChangeStatus status;
        if (outcome.activateImmediately()) {
            user.setSubscriptionPlan(target);
            status = SubscriptionChangeStatus.ACTIVATED;
        } else {
            status = SubscriptionChangeStatus.PENDING_PAYMENT;
        }
        return new ChangeSubscriptionResponse(status, buildMySubscription(user), outcome.checkoutUrl());
    }

    public void assertCanCreateCompany(User owner) {
        SubscriptionPlan plan = owner.getSubscriptionPlan();
        if (plan == null || companyRepository.countByOwnerId(owner.getId()) >= plan.getMaxCompanies()) {
            throw new SubscriptionLimitException("error.subscription.company-limit-reached");
        }
    }

    public void assertCanAddSeat(Company company) {
        int limit = seatLimitFor(company);
        if (seatsUsed(company.getId()) >= limit) {
            throw new SubscriptionLimitException("error.subscription.seat-limit-reached", limit);
        }
    }

    public CompanySeatUsage seatUsage(Company company) {
        long activeProfiles = profileRepository.countByCompanyIdAndActiveTrue(company.getId());
        long members = companyMemberRepository.countByCompanyId(company.getId());
        long used = activeProfiles + members;
        int limit = seatLimitFor(company);
        long remaining = Math.max(0, limit - used);
        return new CompanySeatUsage(company.getId(), company.getName(), activeProfiles, members, used, limit, remaining);
    }

    private MySubscriptionResponse buildMySubscription(User user) {
        SubscriptionPlan plan = user.getSubscriptionPlan();
        SubscriptionPlanResponse planResponse = plan == null ? null : planMapper.toResponse(plan);
        int companiesLimit = plan == null ? 0 : plan.getMaxCompanies();
        long companiesUsed = companyRepository.countByOwnerId(user.getId());
        List<CompanySeatUsage> companies = companyRepository.findByOwnerId(user.getId()).stream()
                .map(this::seatUsage)
                .toList();
        return new MySubscriptionResponse(planResponse, companiesUsed, companiesLimit, companies);
    }

    private void assertDowngradeFits(User user, SubscriptionPlan target) {
        long ownedCompanies = companyRepository.countByOwnerId(user.getId());
        if (ownedCompanies > target.getMaxCompanies()) {
            throw new SubscriptionLimitException("error.subscription.downgrade-companies",
                    target.getMaxCompanies(), ownedCompanies);
        }
        for (Company company : companyRepository.findByOwnerId(user.getId())) {
            long used = seatsUsed(company.getId());
            if (used > target.getMaxSeatsPerCompany()) {
                throw new SubscriptionLimitException("error.subscription.downgrade-seats",
                        company.getName(), used, target.getMaxSeatsPerCompany());
            }
        }
    }

    private long seatsUsed(UUID companyId) {
        return profileRepository.countByCompanyIdAndActiveTrue(companyId)
                + companyMemberRepository.countByCompanyId(companyId);
    }

    private int seatLimitFor(Company company) {
        SubscriptionPlan plan = company.getOwner().getSubscriptionPlan();
        return plan == null ? 0 : plan.getMaxSeatsPerCompany();
    }

    private SubscriptionPlan loadPlan(String planCode) {
        SubscriptionPlanCode code;
        try {
            code = SubscriptionPlanCode.valueOf(planCode.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new ResourceNotFoundException("error.subscription.plan-not-found");
        }
        return planRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("error.subscription.plan-not-found"));
    }
}
