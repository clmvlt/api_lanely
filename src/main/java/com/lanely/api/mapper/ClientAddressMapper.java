package com.lanely.api.mapper;

import com.lanely.api.dto.client.ClientAddressResponse;
import com.lanely.api.dto.company.AddressDto;
import com.lanely.api.entity.ClientAddress;
import com.lanely.api.entity.embeddable.Address;

public final class ClientAddressMapper {

    private ClientAddressMapper() {
    }

    public static ClientAddressResponse toResponse(ClientAddress address) {
        return new ClientAddressResponse(address.getId(), address.getLabel(), address.getType(),
                CompanyMapper.toAddressDto(address.getAddress()), address.getLatitude(), address.getLongitude(),
                address.isPrimary(), address.isDefaultBilling(), address.isDefaultShipping(),
                address.getContactName(), address.getContactPhone(), address.getContactEmail(),
                address.getDeliveryInstructions(), address.isActive(), address.getCreatedAt(), address.getUpdatedAt());
    }

    public static Address toAddress(AddressDto dto, String fallbackCountry) {
        Address address = new Address();
        if (dto != null) {
            address.setLine1(CompanyMapper.blankToNull(dto.line1()));
            address.setLine2(CompanyMapper.blankToNull(dto.line2()));
            address.setPostalCode(CompanyMapper.blankToNull(dto.postalCode()));
            address.setCity(CompanyMapper.blankToNull(dto.city()));
            address.setState(CompanyMapper.blankToNull(dto.state()));
            address.setCountry(resolveCountry(dto.country(), fallbackCountry));
        } else {
            address.setCountry(normalizeCountry(fallbackCountry));
        }
        return address;
    }

    private static String resolveCountry(String country, String fallbackCountry) {
        String trimmed = CompanyMapper.blankToNull(country);
        return trimmed == null ? normalizeCountry(fallbackCountry) : trimmed.toUpperCase();
    }

    private static String normalizeCountry(String fallbackCountry) {
        String trimmed = CompanyMapper.blankToNull(fallbackCountry);
        return trimmed == null ? "FR" : trimmed.toUpperCase();
    }
}
