package com.lanely.api.mapper;

import com.lanely.api.dto.vehicle.InsuranceInfoDto;
import com.lanely.api.dto.vehicle.VehicleResponse;
import com.lanely.api.dto.vehicle.VehicleSummaryResponse;
import com.lanely.api.entity.Vehicle;
import com.lanely.api.entity.VehicleDocument;
import com.lanely.api.entity.embeddable.InsuranceInfo;

import java.util.List;
import java.util.UUID;

public final class VehicleMapper {

    private VehicleMapper() {
    }

    public static VehicleSummaryResponse toSummary(Vehicle vehicle) {
        return new VehicleSummaryResponse(vehicle.getId(), vehicle.getRegistrationPlate(), vehicle.getMake(),
                vehicle.getModel(), vehicle.getVehicleType(), vehicle.getStatus(), vehicle.getLatestMileageKm(),
                vehicle.getCreatedAt());
    }

    public static VehicleResponse toResponse(Vehicle vehicle, List<VehicleDocument> documents, UUID companyId) {
        return new VehicleResponse(vehicle.getId(), vehicle.getRegistrationPlate(), vehicle.getVin(), vehicle.getMake(),
                vehicle.getModel(), vehicle.getVersion(), vehicle.getVehicleType(), vehicle.getFuelType(),
                vehicle.getFirstRegistrationDate(), vehicle.getEmissionClass(), vehicle.getGrossWeightKg(),
                vehicle.getPayloadKg(), vehicle.getRegistrationCertificateNumber(),
                toInsuranceDto(vehicle.getInsuranceInfo()), vehicle.getTechnicalInspectionDate(),
                vehicle.getRoadTaxDueDate(), vehicle.getLatestMileageKm(), vehicle.getLatestMileageAt(),
                vehicle.getStatus(), vehicle.getNotes(),
                documents.stream().map(doc -> VehicleDocumentMapper.toResponse(doc, companyId, vehicle.getId())).toList(),
                vehicle.getCreatedAt(), vehicle.getUpdatedAt());
    }

    public static InsuranceInfoDto toInsuranceDto(InsuranceInfo info) {
        if (info == null) {
            return null;
        }
        return new InsuranceInfoDto(info.getInsurerName(), info.getPolicyNumber(), info.getCoverageType(),
                info.getStartDate(), info.getEndDate(), info.getContact());
    }

    public static InsuranceInfo newInsurance(InsuranceInfoDto dto) {
        InsuranceInfo info = new InsuranceInfo();
        applyInsurance(info, dto);
        return info;
    }

    public static void applyInsurance(InsuranceInfo info, InsuranceInfoDto dto) {
        if (dto == null) {
            return;
        }
        info.setInsurerName(CompanyMapper.blankToNull(dto.insurerName()));
        info.setPolicyNumber(CompanyMapper.blankToNull(dto.policyNumber()));
        info.setCoverageType(CompanyMapper.blankToNull(dto.coverageType()));
        info.setStartDate(dto.startDate());
        info.setEndDate(dto.endDate());
        info.setContact(CompanyMapper.blankToNull(dto.contact()));
    }
}
