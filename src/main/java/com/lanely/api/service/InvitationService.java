package com.lanely.api.service;

import com.lanely.api.config.AppMailProperties;
import com.lanely.api.config.InvitationProperties;
import com.lanely.api.config.SiteProperties;
import com.lanely.api.dto.auth.TokenResponse;
import com.lanely.api.dto.auth.UserAuthResponse;
import com.lanely.api.dto.invitation.AcceptInvitationRequest;
import com.lanely.api.dto.invitation.AcceptInvitationResponse;
import com.lanely.api.dto.invitation.InvitationResponse;
import com.lanely.api.dto.invitation.InviteUserRequest;
import com.lanely.api.entity.Company;
import com.lanely.api.entity.CompanyInvitation;
import com.lanely.api.entity.CompanyMember;
import com.lanely.api.entity.User;
import com.lanely.api.entity.enums.CompanyRole;
import com.lanely.api.entity.enums.InvitationStatus;
import com.lanely.api.entity.enums.Permission;
import com.lanely.api.exception.AlreadyMemberException;
import com.lanely.api.exception.BadRequestException;
import com.lanely.api.exception.InvitationExpiredException;
import com.lanely.api.exception.InvitationNotPendingException;
import com.lanely.api.exception.ResourceNotFoundException;
import com.lanely.api.mapper.InvitationMapper;
import com.lanely.api.mapper.UserMapper;
import com.lanely.api.repository.CompanyInvitationRepository;
import com.lanely.api.repository.CompanyMemberRepository;
import com.lanely.api.repository.UserRepository;
import com.lanely.api.service.mail.MailService;
import com.lanely.api.service.support.CodeGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class InvitationService {

    private final CompanyService companyService;
    private final SubscriptionService subscriptionService;
    private final UserService userService;
    private final SessionService sessionService;
    private final EmailVerificationService emailVerificationService;
    private final CompanyInvitationRepository invitationRepository;
    private final CompanyMemberRepository companyMemberRepository;
    private final UserRepository userRepository;
    private final MailService mailService;
    private final CodeGenerator codeGenerator;
    private final long ttlDays;
    private final String baseUrl;
    private final boolean exposeCodeInResponse;

    public InvitationService(CompanyService companyService, SubscriptionService subscriptionService,
                             UserService userService, SessionService sessionService,
                             EmailVerificationService emailVerificationService,
                             CompanyInvitationRepository invitationRepository, CompanyMemberRepository companyMemberRepository,
                             UserRepository userRepository, MailService mailService, CodeGenerator codeGenerator,
                             InvitationProperties invitationProperties, AppMailProperties mailProperties,
                             SiteProperties siteProperties) {
        this.companyService = companyService;
        this.subscriptionService = subscriptionService;
        this.userService = userService;
        this.sessionService = sessionService;
        this.emailVerificationService = emailVerificationService;
        this.invitationRepository = invitationRepository;
        this.companyMemberRepository = companyMemberRepository;
        this.userRepository = userRepository;
        this.mailService = mailService;
        this.codeGenerator = codeGenerator;
        this.ttlDays = invitationProperties.ttlDays();
        this.baseUrl = siteProperties.url();
        this.exposeCodeInResponse = mailProperties.exposeCodeInResponse();
    }

    @Transactional
    public InvitationResponse invite(UUID currentUserId, UUID companyId, InviteUserRequest request) {
        CompanyMember membership = companyService.requirePermission(companyId, currentUserId, Permission.MANAGE_COMPANY);
        Company company = membership.getCompany();
        String email = request.email().trim().toLowerCase();

        userRepository.findByEmailIgnoreCase(email).ifPresent(existing -> {
            if (companyMemberRepository.existsByCompanyIdAndUserId(companyId, existing.getId())) {
                throw new AlreadyMemberException("error.invitation.already-member");
            }
        });
        if (invitationRepository.existsByCompanyIdAndEmailIgnoreCaseAndStatus(companyId, email, InvitationStatus.PENDING)) {
            throw new AlreadyMemberException("error.invitation.pending-exists");
        }

        String code = generateUniqueCode();
        CompanyInvitation invitation = new CompanyInvitation();
        invitation.setCompany(company);
        invitation.setEmail(email);
        invitation.setCode(code);
        invitation.setStatus(InvitationStatus.PENDING);
        invitation.setInvitedBy(membership.getUser());
        invitation.setExpiresAt(Instant.now().plusSeconds(ttlDays * 86400L));
        invitationRepository.save(invitation);

        String joinLink = buildJoinLink(code);
        mailService.sendInvitation(email, company.getName(), code, joinLink);

        if (exposeCodeInResponse) {
            return InvitationMapper.toResponse(invitation, code, joinLink);
        }
        return InvitationMapper.toResponse(invitation, null, null);
    }

    @Transactional(readOnly = true)
    public List<InvitationResponse> listInvitations(UUID currentUserId, UUID companyId) {
        companyService.requirePermission(companyId, currentUserId, Permission.MANAGE_COMPANY);
        boolean expose = exposeCodeInResponse;
        return invitationRepository.findByCompanyId(companyId).stream()
                .map(invitation -> expose
                        ? InvitationMapper.toResponse(invitation, invitation.getCode(), buildJoinLink(invitation.getCode()))
                        : InvitationMapper.toResponse(invitation, null, null))
                .toList();
    }

    @Transactional
    public void deleteInvitation(UUID currentUserId, UUID companyId, UUID invitationId) {
        companyService.requirePermission(companyId, currentUserId, Permission.MANAGE_COMPANY);
        CompanyInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new ResourceNotFoundException("error.invitation.not-found"));
        if (!invitation.getCompany().getId().equals(companyId)) {
            throw new ResourceNotFoundException("error.invitation.not-found");
        }
        invitationRepository.delete(invitation);
    }

    @Transactional
    public AcceptInvitationResponse accept(UUID currentUserId, AcceptInvitationRequest request, DeviceMeta deviceMeta) {
        CompanyInvitation invitation = invitationRepository.findByCode(request.code().trim())
                .orElseThrow(() -> new ResourceNotFoundException("error.invitation.not-found"));

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new InvitationNotPendingException("error.invitation.not-pending");
        }
        if (invitation.getExpiresAt().isBefore(Instant.now())) {
            invitation.setStatus(InvitationStatus.EXPIRED);
            throw new InvitationExpiredException("error.invitation.expired");
        }

        boolean newAccountCreated = false;
        User user;
        if (currentUserId != null) {
            user = userService.getByIdOrThrow(currentUserId);
        } else if (request.newAccount() != null) {
            user = userService.register(request.newAccount());
            newAccountCreated = true;
        } else {
            throw new BadRequestException("error.invitation.auth-required");
        }

        Company company = invitation.getCompany();
        if (companyMemberRepository.existsByCompanyIdAndUserId(company.getId(), user.getId())) {
            throw new AlreadyMemberException("error.invitation.already-member-self");
        }

        subscriptionService.assertCanAddSeat(company);

        CompanyMember member = new CompanyMember();
        member.setCompany(company);
        member.setUser(user);
        member.setRole(CompanyRole.MEMBER);
        companyMemberRepository.save(member);

        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitation.setAcceptedBy(user);

        UserAuthResponse auth = null;
        if (newAccountCreated) {
            if (invitation.getEmail().equalsIgnoreCase(user.getEmail())) {
                user.setEmailVerified(true);
            } else {
                emailVerificationService.createAndSend(user);
            }
            TokenResponse tokens = sessionService.create(user, deviceMeta);
            auth = new UserAuthResponse(tokens, UserMapper.toSummary(user));
        }
        return new AcceptInvitationResponse(company.getId(), company.getName(), CompanyRole.MEMBER, auth);
    }

    private String generateUniqueCode() {
        String code;
        do {
            code = codeGenerator.invitationCode();
        } while (invitationRepository.findByCode(code).isPresent());
        return code;
    }

    private String buildJoinLink(String code) {
        return baseUrl + "/join?code=" + code;
    }
}
