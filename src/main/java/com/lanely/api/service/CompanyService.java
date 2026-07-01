package com.lanely.api.service;

import com.lanely.api.dto.company.AddressDto;
import com.lanely.api.dto.company.CompanyCodeResponse;
import com.lanely.api.dto.company.CompanyMeResponse;
import com.lanely.api.dto.company.CompanyResponse;
import com.lanely.api.dto.company.CreateCompanyRequest;
import com.lanely.api.dto.company.DepositAddressDto;
import com.lanely.api.dto.company.LegalInfoDto;
import com.lanely.api.dto.company.PublicCompanyResponse;
import com.lanely.api.dto.company.UpdateCompanyRequest;
import com.lanely.api.dto.member.MemberResponse;
import com.lanely.api.dto.permission.MemberPermissionsResponse;
import com.lanely.api.dto.permission.PermissionDto;
import com.lanely.api.dto.permission.UpdateMemberPermissionsRequest;
import com.lanely.api.dto.subscription.CompanySeatUsage;
import com.lanely.api.entity.Company;
import com.lanely.api.entity.CompanyMember;
import com.lanely.api.entity.Image;
import com.lanely.api.entity.User;
import com.lanely.api.entity.enums.CompanyRole;
import com.lanely.api.entity.enums.Permission;
import com.lanely.api.exception.BadRequestException;
import com.lanely.api.exception.MissingPermissionException;
import com.lanely.api.exception.NotCompanyOwnerException;
import com.lanely.api.exception.ResourceNotFoundException;
import com.lanely.api.mapper.CompanyMapper;
import com.lanely.api.mapper.MemberMapper;
import com.lanely.api.repository.CompanyMemberRepository;
import com.lanely.api.repository.CompanyRepository;
import com.lanely.api.repository.UserRepository;
import com.lanely.api.service.support.CodeGenerator;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyMemberRepository companyMemberRepository;
    private final UserRepository userRepository;
    private final CodeGenerator codeGenerator;
    private final ImageService imageService;
    private final SubscriptionService subscriptionService;

    public CompanyService(CompanyRepository companyRepository, CompanyMemberRepository companyMemberRepository,
                          UserRepository userRepository, CodeGenerator codeGenerator, ImageService imageService,
                          SubscriptionService subscriptionService) {
        this.companyRepository = companyRepository;
        this.companyMemberRepository = companyMemberRepository;
        this.userRepository = userRepository;
        this.codeGenerator = codeGenerator;
        this.imageService = imageService;
        this.subscriptionService = subscriptionService;
    }

    @Transactional
    public CompanyResponse createCompany(UUID currentUserId, CreateCompanyRequest request) {
        User owner = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("error.user.not-found"));

        subscriptionService.assertCanCreateCompany(owner);

        Company company = new Company();
        company.setPublicCode(generateUniqueCode());
        company.setOwner(owner);
        applyEditableInfo(company, request.name(), request.legalInfo(), request.billingAddress(),
                request.depositAddress(), request.billingEmail(), request.billingPhone());
        companyRepository.save(company);

        CompanyMember member = new CompanyMember();
        member.setCompany(company);
        member.setUser(owner);
        member.setRole(CompanyRole.OWNER);
        companyMemberRepository.save(member);

        return CompanyMapper.toResponse(company, CompanyRole.OWNER);
    }

    @Transactional(readOnly = true)
    public CompanyResponse getCompany(UUID currentUserId, UUID companyId) {
        CompanyMember member = requireMember(companyId, currentUserId);
        return CompanyMapper.toResponse(member.getCompany(), member.getRole());
    }

    @Transactional(readOnly = true)
    public CompanyCodeResponse getCompanyCode(UUID currentUserId, UUID companyId) {
        CompanyMember member = requireMember(companyId, currentUserId);
        return CompanyMapper.toCodeResponse(member.getCompany());
    }

    @Transactional(readOnly = true)
    public CompanySeatUsage getSeatUsage(UUID currentUserId, UUID companyId) {
        CompanyMember member = requireMember(companyId, currentUserId);
        return subscriptionService.seatUsage(member.getCompany());
    }

    @Transactional(readOnly = true)
    public PublicCompanyResponse getByPublicCode(String publicCode) {
        Company company = companyRepository.findByPublicCode(publicCode.trim())
                .orElseThrow(() -> new ResourceNotFoundException("error.company.not-found-for-code"));
        return CompanyMapper.toPublicResponse(company);
    }

    @Transactional(readOnly = true)
    public List<MemberResponse> listMembers(UUID currentUserId, UUID companyId) {
        requireMember(companyId, currentUserId);
        return companyMemberRepository.findByCompanyId(companyId).stream()
                .map(MemberMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CompanyMember requireMember(UUID companyId, UUID userId) {
        ensureCompanyExists(companyId);
        return companyMemberRepository.findByCompanyIdAndUserId(companyId, userId)
                .orElseThrow(() -> new AccessDeniedException("You are not a member of this company"));
    }

    @Transactional(readOnly = true)
    public CompanyMember requireOwner(UUID companyId, UUID userId) {
        CompanyMember member = requireMember(companyId, userId);
        if (member.getRole() != CompanyRole.OWNER) {
            throw new NotCompanyOwnerException("error.company.not-owner");
        }
        return member;
    }

    @Transactional(readOnly = true)
    public CompanyMember requirePermission(UUID companyId, UUID userId, Permission permission) {
        CompanyMember member = requireMember(companyId, userId);
        if (member.getRole() == CompanyRole.OWNER || member.getPermissions().contains(permission)) {
            return member;
        }
        throw new MissingPermissionException("error.permission.missing", permission.name());
    }

    @Transactional
    public CompanyResponse updateCompany(UUID currentUserId, UUID companyId, UpdateCompanyRequest request) {
        CompanyMember member = requirePermission(companyId, currentUserId, Permission.MANAGE_COMPANY);
        Company company = member.getCompany();
        applyEditableInfo(company, request.name(), request.legalInfo(), request.billingAddress(),
                request.depositAddress(), request.billingEmail(), request.billingPhone());
        return CompanyMapper.toResponse(company, member.getRole());
    }

    private void applyEditableInfo(Company company, String name, LegalInfoDto legalInfo, AddressDto billingAddress,
                                   DepositAddressDto depositAddress, String billingEmail, String billingPhone) {
        company.setName(name.trim());
        company.setLegalInfo(CompanyMapper.toLegalInfo(legalInfo));
        company.setBillingAddress(CompanyMapper.toAddress(billingAddress));
        company.setDepositAddress(CompanyMapper.toDepositAddress(depositAddress));
        company.setDepositCoordinate(CompanyMapper.toDepositCoordinate(depositAddress));
        company.setBillingEmail(CompanyMapper.blankToNull(billingEmail));
        company.setBillingPhone(CompanyMapper.blankToNull(billingPhone));
    }

    @Transactional
    public CompanyResponse setCompanyPicture(UUID currentUserId, UUID companyId, MultipartFile file) {
        CompanyMember member = requirePermission(companyId, currentUserId, Permission.MANAGE_COMPANY);
        Company company = member.getCompany();
        Image previous = company.getProfileImage();
        company.setProfileImage(imageService.upload(file));
        if (previous != null) {
            imageService.delete(previous);
        }
        return CompanyMapper.toResponse(company, member.getRole());
    }

    @Transactional
    public CompanyResponse removeCompanyPicture(UUID currentUserId, UUID companyId) {
        CompanyMember member = requirePermission(companyId, currentUserId, Permission.MANAGE_COMPANY);
        Company company = member.getCompany();
        Image previous = company.getProfileImage();
        if (previous != null) {
            company.setProfileImage(null);
            imageService.delete(previous);
        }
        return CompanyMapper.toResponse(company, member.getRole());
    }

    @Transactional(readOnly = true)
    public CompanyMeResponse getMe(UUID currentUserId, UUID companyId) {
        CompanyMember member = requireMember(companyId, currentUserId);
        return new CompanyMeResponse(companyId, member.getRole(), MemberMapper.effectivePermissions(member));
    }

    @Transactional(readOnly = true)
    public MemberPermissionsResponse getMemberPermissions(UUID currentUserId, UUID companyId, UUID targetUserId) {
        requirePermission(companyId, currentUserId, Permission.MANAGE_PERMISSIONS);
        CompanyMember target = loadMember(companyId, targetUserId);
        return new MemberPermissionsResponse(targetUserId, target.getRole(), MemberMapper.effectivePermissions(target));
    }

    @Transactional
    public MemberPermissionsResponse setMemberPermissions(UUID currentUserId, UUID companyId, UUID targetUserId,
                                                          UpdateMemberPermissionsRequest request) {
        requirePermission(companyId, currentUserId, Permission.MANAGE_PERMISSIONS);
        CompanyMember target = loadMember(companyId, targetUserId);
        if (target.getRole() == CompanyRole.OWNER) {
            throw new BadRequestException("error.owner.all-permissions");
        }
        Set<Permission> permissions = request.permissions().isEmpty()
                ? EnumSet.noneOf(Permission.class)
                : EnumSet.copyOf(request.permissions());
        target.setPermissions(permissions);
        return new MemberPermissionsResponse(targetUserId, target.getRole(), MemberMapper.effectivePermissions(target));
    }

    public List<PermissionDto> permissionCatalog() {
        return Arrays.stream(Permission.values())
                .map(permission -> new PermissionDto(permission.name(), permission.getDescription()))
                .toList();
    }

    private CompanyMember loadMember(UUID companyId, UUID userId) {
        ensureCompanyExists(companyId);
        return companyMemberRepository.findByCompanyIdAndUserId(companyId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("error.member.not-found"));
    }

    private void ensureCompanyExists(UUID companyId) {
        if (!companyRepository.existsById(companyId)) {
            throw new ResourceNotFoundException("error.company.not-found");
        }
    }

    private String generateUniqueCode() {
        String code;
        do {
            code = codeGenerator.companyCode();
        } while (companyRepository.existsByPublicCode(code));
        return code;
    }
}
