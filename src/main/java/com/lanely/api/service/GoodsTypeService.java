package com.lanely.api.service;

import com.lanely.api.dto.common.PageResponse;
import com.lanely.api.dto.goodstype.CreateGoodsTypeRequest;
import com.lanely.api.dto.goodstype.GoodsTypeResponse;
import com.lanely.api.dto.goodstype.UpdateGoodsTypeRequest;
import com.lanely.api.entity.Company;
import com.lanely.api.entity.CompanyMember;
import com.lanely.api.entity.GoodsType;
import com.lanely.api.entity.enums.Permission;
import com.lanely.api.exception.BadRequestException;
import com.lanely.api.exception.GoodsTypeNameTakenException;
import com.lanely.api.exception.ResourceNotFoundException;
import com.lanely.api.mapper.CompanyMapper;
import com.lanely.api.mapper.GoodsTypeMapper;
import com.lanely.api.repository.GoodsTypeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class GoodsTypeService {

    private static final Set<String> SORTABLE_FIELDS = Set.of("name", "createdAt", "updatedAt");

    private final CompanyService companyService;
    private final GoodsTypeRepository goodsTypeRepository;

    public GoodsTypeService(CompanyService companyService, GoodsTypeRepository goodsTypeRepository) {
        this.companyService = companyService;
        this.goodsTypeRepository = goodsTypeRepository;
    }

    @Transactional
    public GoodsTypeResponse createGoodsType(UUID currentUserId, UUID companyId, CreateGoodsTypeRequest request) {
        CompanyMember membership = companyService.requirePermission(companyId, currentUserId, Permission.MANAGE_TRANSPORTS);
        Company company = membership.getCompany();

        String name = request.name().trim();
        if (goodsTypeRepository.existsByCompanyIdAndNameIgnoreCase(companyId, name)) {
            throw new GoodsTypeNameTakenException("error.goods-type.name-taken");
        }

        GoodsType goodsType = new GoodsType();
        goodsType.setCompany(company);
        goodsType.setName(name);
        goodsType.setDescription(CompanyMapper.blankToNull(request.description()));
        goodsType.setPackagingType(CompanyMapper.blankToNull(request.packagingType()));
        goodsType.setNumberOfPackages(request.numberOfPackages());
        goodsType.setGrossWeightKg(request.grossWeightKg());
        goodsType.setVolumeM3(request.volumeM3());
        goodsType.setLengthCm(request.lengthCm());
        goodsType.setWidthCm(request.widthCm());
        goodsType.setHeightCm(request.heightCm());
        goodsType.setDangerousGoods(request.dangerousGoods() != null && request.dangerousGoods());
        goodsType.setUnNumber(CompanyMapper.blankToNull(request.unNumber()));
        goodsTypeRepository.save(goodsType);

        return GoodsTypeMapper.toResponse(goodsType);
    }

    @Transactional(readOnly = true)
    public PageResponse<GoodsTypeResponse> listGoodsTypes(UUID currentUserId, UUID companyId, String q, Pageable pageable) {
        companyService.requireMember(companyId, currentUserId);
        validateSort(pageable.getSort());
        Page<GoodsTypeResponse> page = goodsTypeRepository
                .search(companyId, searchPattern(q), pageable)
                .map(GoodsTypeMapper::toResponse);
        return PageResponse.of(page);
    }

    @Transactional(readOnly = true)
    public GoodsTypeResponse getGoodsType(UUID currentUserId, UUID companyId, UUID goodsTypeId) {
        companyService.requireMember(companyId, currentUserId);
        return GoodsTypeMapper.toResponse(loadGoodsType(companyId, goodsTypeId));
    }

    @Transactional
    public GoodsTypeResponse updateGoodsType(UUID currentUserId, UUID companyId, UUID goodsTypeId,
                                             UpdateGoodsTypeRequest request) {
        companyService.requirePermission(companyId, currentUserId, Permission.MANAGE_TRANSPORTS);
        GoodsType goodsType = loadGoodsType(companyId, goodsTypeId);

        if (request.name() != null) {
            String name = request.name().trim();
            if (!name.equalsIgnoreCase(goodsType.getName())
                    && goodsTypeRepository.existsByCompanyIdAndNameIgnoreCase(companyId, name)) {
                throw new GoodsTypeNameTakenException("error.goods-type.name-taken");
            }
            goodsType.setName(name);
        }
        if (request.description() != null) {
            goodsType.setDescription(CompanyMapper.blankToNull(request.description()));
        }
        if (request.packagingType() != null) {
            goodsType.setPackagingType(CompanyMapper.blankToNull(request.packagingType()));
        }
        if (request.numberOfPackages() != null) {
            goodsType.setNumberOfPackages(request.numberOfPackages());
        }
        if (request.grossWeightKg() != null) {
            goodsType.setGrossWeightKg(request.grossWeightKg());
        }
        if (request.volumeM3() != null) {
            goodsType.setVolumeM3(request.volumeM3());
        }
        if (request.lengthCm() != null) {
            goodsType.setLengthCm(request.lengthCm());
        }
        if (request.widthCm() != null) {
            goodsType.setWidthCm(request.widthCm());
        }
        if (request.heightCm() != null) {
            goodsType.setHeightCm(request.heightCm());
        }
        if (request.dangerousGoods() != null) {
            goodsType.setDangerousGoods(request.dangerousGoods());
        }
        if (request.unNumber() != null) {
            goodsType.setUnNumber(CompanyMapper.blankToNull(request.unNumber()));
        }

        return GoodsTypeMapper.toResponse(goodsType);
    }

    @Transactional
    public void deleteGoodsType(UUID currentUserId, UUID companyId, UUID goodsTypeId) {
        companyService.requirePermission(companyId, currentUserId, Permission.MANAGE_TRANSPORTS);
        GoodsType goodsType = loadGoodsType(companyId, goodsTypeId);
        goodsTypeRepository.delete(goodsType);
    }

    private GoodsType loadGoodsType(UUID companyId, UUID goodsTypeId) {
        return goodsTypeRepository.findByIdAndCompanyId(goodsTypeId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("error.goods-type.not-found"));
    }

    private void validateSort(Sort sort) {
        for (Sort.Order order : sort) {
            if (!SORTABLE_FIELDS.contains(order.getProperty())) {
                throw new BadRequestException("error.sort.invalid", order.getProperty());
            }
        }
    }

    private String searchPattern(String q) {
        String trimmed = CompanyMapper.blankToNull(q);
        return trimmed == null ? "%" : "%" + trimmed.toLowerCase(Locale.ROOT) + "%";
    }
}
