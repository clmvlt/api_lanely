package com.lanely.api.mapper;

import com.lanely.api.dto.waybill.GoodsLineResponse;
import com.lanely.api.dto.waybill.PlaceResponse;
import com.lanely.api.dto.waybill.SignatureResponse;
import com.lanely.api.dto.waybill.WaybillPartyResponse;
import com.lanely.api.dto.waybill.WaybillResponse;
import com.lanely.api.dto.waybill.WaybillSummaryResponse;
import com.lanely.api.entity.Tour;
import com.lanely.api.entity.Waybill;
import com.lanely.api.entity.WaybillGoodsLine;
import com.lanely.api.entity.WaybillParty;
import com.lanely.api.entity.WaybillSignature;
import com.lanely.api.entity.embeddable.Address;
import com.lanely.api.entity.embeddable.GeoPoint;
import com.lanely.api.entity.enums.WaybillPartyRole;

import java.time.Instant;
import java.util.UUID;

public final class WaybillMapper {

    private WaybillMapper() {
    }

    public static WaybillSummaryResponse toSummary(Waybill waybill) {
        WaybillParty shipper = waybill.getParty(WaybillPartyRole.SHIPPER);
        WaybillParty consignee = waybill.getParty(WaybillPartyRole.CONSIGNEE);
        return new WaybillSummaryResponse(waybill.getId(), waybill.getReference(), waybill.getStatus(),
                waybill.getScope(), waybill.getClient().getName(),
                shipper == null ? null : shipper.getName(),
                consignee == null ? null : consignee.getName(),
                city(waybill.getPlaceOfTakingOver()),
                latitude(waybill.getTakingOverLocation()), longitude(waybill.getTakingOverLocation()),
                waybill.getTakingOverPlannedAt(),
                city(waybill.getPlaceOfDelivery()),
                latitude(waybill.getDeliveryLocation()), longitude(waybill.getDeliveryLocation()),
                waybill.getDeliveryPlannedAt(), tourId(waybill.getTour()), waybill.getPositionInTour(),
                AssigneeMapper.id(waybill.getAssignedAccount()), AssigneeMapper.type(waybill.getAssignedAccount()),
                AssigneeMapper.name(waybill.getAssignedAccount()),
                waybill.isArchived(), waybill.getArchivedAt(),
                waybill.getDockEnteredAt(), waybill.getDockExitedAt(), waybill.getCreatedAt());
    }

    private static String city(Address address) {
        return address == null ? null : address.getCity();
    }

    private static Double latitude(GeoPoint location) {
        return location == null ? null : location.getLatitude();
    }

    private static Double longitude(GeoPoint location) {
        return location == null ? null : location.getLongitude();
    }

    public static WaybillResponse toResponse(Waybill waybill) {
        return new WaybillResponse(waybill.getId(), waybill.getReference(), waybill.getStatus(), waybill.getScope(),
                waybill.getClient().getId(), waybill.getClient().getName(),
                toPartyResponse(waybill.getParty(WaybillPartyRole.SHIPPER)),
                toPartyResponse(waybill.getParty(WaybillPartyRole.CONSIGNEE)),
                toPlaceResponse(waybill.getPlaceOfTakingOver(), waybill.getTakingOverLocation(),
                        waybill.getPickupClientId(), waybill.getPickupClientAddressId(),
                        waybill.getTakingOverPlannedAt(), waybill.getTakingOverActualAt()),
                toPlaceResponse(waybill.getPlaceOfDelivery(), waybill.getDeliveryLocation(),
                        waybill.getDeliveryClientId(), waybill.getDeliveryClientAddressId(),
                        waybill.getDeliveryPlannedAt(), waybill.getDeliveryActualAt()),
                waybill.getGoodsLines().stream().map(WaybillMapper::toGoodsResponse).toList(),
                waybill.getSignatures().stream().map(WaybillMapper::toSignatureResponse).toList(),
                waybill.getAttachedDocuments(), waybill.getSenderInstructions(),
                waybill.getCarriageChargesAmount(), waybill.getCarriageChargesCurrency(),
                waybill.getReservationsAndObservations(),
                tourId(waybill.getTour()), waybill.getPositionInTour(),
                AssigneeMapper.id(waybill.getAssignedAccount()), AssigneeMapper.type(waybill.getAssignedAccount()),
                AssigneeMapper.name(waybill.getAssignedAccount()), GeoMapper.toRouteInfoDto(waybill.getRoute()),
                waybill.getFailureReason(), ImageMapper.url(waybill.getProofOfDeliveryImage()), waybill.getNotes(),
                waybill.isArchived(), waybill.getArchivedAt(),
                waybill.getDockEnteredAt(), waybill.getDockExitedAt(),
                waybill.getCreatedAt(), waybill.getUpdatedAt());
    }

    private static WaybillPartyResponse toPartyResponse(WaybillParty party) {
        if (party == null) {
            return null;
        }
        return new WaybillPartyResponse(party.getRole(), party.getName(),
                CompanyMapper.toAddressDto(party.getAddress()), GeoMapper.toCoordinateDto(party.getLocation()),
                party.getContactName(), party.getContactPhone(), party.getContactEmail(),
                CompanyMapper.toLegalInfoDto(party.getLegalInfo()), party.getClientId(), party.getClientAddressId());
    }

    private static PlaceResponse toPlaceResponse(Address address, GeoPoint location, UUID clientId, UUID clientAddressId,
                                                 Instant plannedAt, Instant actualAt) {
        return new PlaceResponse(CompanyMapper.toAddressDto(address), GeoMapper.toCoordinateDto(location),
                clientId, clientAddressId, plannedAt, actualAt);
    }

    private static GoodsLineResponse toGoodsResponse(WaybillGoodsLine line) {
        return new GoodsLineResponse(line.getId(), line.getPosition(), line.getStatus(), line.getDescription(),
                line.getPackagingType(), line.getNumberOfPackages(), line.getMarksAndNumbers(), line.getGrossWeightKg(),
                line.getVolumeM3(), line.getLengthCm(), line.getWidthCm(), line.getHeightCm(),
                line.isDangerousGoods(), line.getUnNumber(),
                line.getDockEnteredAt(), line.getDockExitedAt());
    }

    private static SignatureResponse toSignatureResponse(WaybillSignature signature) {
        return new SignatureResponse(signature.getId(), signature.getRole(), signature.getSignerName(),
                signature.getSignedAt(), signature.getPlace(), signature.getMethod(),
                ImageMapper.url(signature.getSignatureImage()));
    }

    private static UUID tourId(Tour tour) {
        return tour == null ? null : tour.getId();
    }
}
