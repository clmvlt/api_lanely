package com.lanely.api.mapper;

import com.lanely.api.dto.vehicle.VehicleDocumentResponse;
import com.lanely.api.entity.Account;
import com.lanely.api.entity.Document;
import com.lanely.api.entity.VehicleDocument;

import java.util.UUID;

public final class VehicleDocumentMapper {

    private VehicleDocumentMapper() {
    }

    public static VehicleDocumentResponse toResponse(VehicleDocument vehicleDocument, UUID companyId, UUID vehicleId) {
        Document document = vehicleDocument.getDocument();
        Account uploadedBy = vehicleDocument.getUploadedBy();
        String downloadUrl = "/companies/" + companyId + "/vehicles/" + vehicleId
                + "/documents/" + vehicleDocument.getId() + "/download";
        return new VehicleDocumentResponse(vehicleDocument.getId(), vehicleDocument.getCategory(),
                vehicleDocument.getLabel(), document.getOriginalFilename(), document.getContentType(),
                document.getSizeBytes(), downloadUrl, uploadedBy == null ? null : uploadedBy.getId(),
                vehicleDocument.getCreatedAt());
    }
}
