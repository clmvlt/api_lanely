package com.lanely.api.mapper;

import com.lanely.api.dto.company.AddressDto;
import com.lanely.api.dto.company.CompanyCodeResponse;
import com.lanely.api.dto.company.CompanyResponse;
import com.lanely.api.dto.company.DepositAddressDto;
import com.lanely.api.dto.company.LegalInfoDto;
import com.lanely.api.dto.company.PublicCompanyResponse;
import com.lanely.api.dto.geo.CoordinateDto;
import com.lanely.api.entity.Company;
import com.lanely.api.entity.embeddable.Address;
import com.lanely.api.entity.embeddable.GeoPoint;
import com.lanely.api.entity.embeddable.LegalInfo;
import com.lanely.api.entity.enums.CompanyRole;

public final class CompanyMapper {

    private CompanyMapper() {
    }

    public static CompanyResponse toResponse(Company company, CompanyRole callerRole) {
        return new CompanyResponse(company.getId(), company.getName(), company.getPublicCode(), callerRole,
                ImageMapper.url(company.getProfileImage()), toLegalInfoDto(company.getLegalInfo()),
                toAddressDto(company.getBillingAddress()),
                toDepositAddressDto(company.getDepositAddress(), company.getDepositCoordinate()),
                company.getBillingEmail(), company.getBillingPhone());
    }

    public static CompanyCodeResponse toCodeResponse(Company company) {
        return new CompanyCodeResponse(company.getId(), company.getPublicCode());
    }

    public static PublicCompanyResponse toPublicResponse(Company company) {
        return new PublicCompanyResponse(company.getId(), company.getName(), company.getPublicCode(),
                ImageMapper.url(company.getProfileImage()));
    }

    public static LegalInfoDto toLegalInfoDto(LegalInfo legalInfo) {
        if (legalInfo == null) {
            return null;
        }
        return new LegalInfoDto(legalInfo.getLegalName(), legalInfo.getRegistrationNumber(), legalInfo.getVatNumber(),
                legalInfo.getLegalForm());
    }

    public static AddressDto toAddressDto(Address address) {
        if (address == null) {
            return null;
        }
        return new AddressDto(address.getLine1(), address.getLine2(), address.getPostalCode(), address.getCity(),
                address.getState(), address.getCountry());
    }

    public static LegalInfo toLegalInfo(LegalInfoDto dto) {
        LegalInfo legalInfo = new LegalInfo();
        if (dto != null) {
            legalInfo.setLegalName(blankToNull(dto.legalName()));
            legalInfo.setRegistrationNumber(blankToNull(dto.registrationNumber()));
            legalInfo.setVatNumber(blankToNull(dto.vatNumber()));
            legalInfo.setLegalForm(blankToNull(dto.legalForm()));
        }
        return legalInfo;
    }

    public static Address toAddress(AddressDto dto) {
        Address address = new Address();
        if (dto != null) {
            address.setLine1(blankToNull(dto.line1()));
            address.setLine2(blankToNull(dto.line2()));
            address.setPostalCode(blankToNull(dto.postalCode()));
            address.setCity(blankToNull(dto.city()));
            address.setState(blankToNull(dto.state()));
            address.setCountry(normalizeCountry(dto.country()));
        }
        return address;
    }

    public static DepositAddressDto toDepositAddressDto(Address address, GeoPoint coordinate) {
        AddressDto addressDto = toAddressDto(address);
        CoordinateDto coordinateDto = toCoordinateDto(coordinate);
        if (addressDto == null && coordinateDto == null) {
            return null;
        }
        return new DepositAddressDto(addressDto, coordinateDto);
    }

    public static CoordinateDto toCoordinateDto(GeoPoint coordinate) {
        if (coordinate == null) {
            return null;
        }
        return new CoordinateDto(coordinate.getLatitude(), coordinate.getLongitude());
    }

    public static Address toDepositAddress(DepositAddressDto dto) {
        return toAddress(dto == null ? null : dto.address());
    }

    public static GeoPoint toDepositCoordinate(DepositAddressDto dto) {
        GeoPoint coordinate = new GeoPoint();
        if (dto != null && dto.coordinate() != null) {
            coordinate.setLatitude(dto.coordinate().latitude());
            coordinate.setLongitude(dto.coordinate().longitude());
        }
        return coordinate;
    }

    private static String normalizeCountry(String country) {
        String trimmed = blankToNull(country);
        return trimmed == null ? "FR" : trimmed.toUpperCase();
    }

    public static String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
