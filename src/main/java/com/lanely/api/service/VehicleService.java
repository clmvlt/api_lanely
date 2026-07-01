package com.lanely.api.service;

import com.lanely.api.dto.common.PageResponse;
import com.lanely.api.dto.vehicle.CreateMileageReadingRequest;
import com.lanely.api.dto.vehicle.CreateVehicleRequest;
import com.lanely.api.dto.vehicle.MileageReadingResponse;
import com.lanely.api.dto.vehicle.UpdateVehicleRequest;
import com.lanely.api.dto.vehicle.VehicleDocumentResponse;
import com.lanely.api.dto.vehicle.VehicleResponse;
import com.lanely.api.dto.vehicle.VehicleSummaryResponse;
import com.lanely.api.entity.Company;
import com.lanely.api.entity.CompanyMember;
import com.lanely.api.entity.Document;
import com.lanely.api.entity.Vehicle;
import com.lanely.api.entity.VehicleDocument;
import com.lanely.api.entity.VehicleMileageReading;
import com.lanely.api.entity.enums.Permission;
import com.lanely.api.entity.enums.VehicleDocumentCategory;
import com.lanely.api.entity.enums.VehicleStatus;
import com.lanely.api.entity.enums.VehicleType;
import com.lanely.api.exception.BadRequestException;
import com.lanely.api.exception.ResourceNotFoundException;
import com.lanely.api.exception.VehiclePlateTakenException;
import com.lanely.api.mapper.CompanyMapper;
import com.lanely.api.mapper.VehicleDocumentMapper;
import com.lanely.api.mapper.VehicleMapper;
import com.lanely.api.mapper.VehicleMileageReadingMapper;
import com.lanely.api.repository.AccountRepository;
import com.lanely.api.repository.VehicleDocumentRepository;
import com.lanely.api.repository.VehicleMileageReadingRepository;
import com.lanely.api.repository.VehicleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class VehicleService {

    private static final Set<String> SORTABLE_FIELDS =
            Set.of("registrationPlate", "make", "model", "createdAt", "updatedAt", "status");

    private final CompanyService companyService;
    private final VehicleRepository vehicleRepository;
    private final VehicleDocumentRepository vehicleDocumentRepository;
    private final VehicleMileageReadingRepository vehicleMileageReadingRepository;
    private final DocumentService documentService;
    private final AccountRepository accountRepository;

    public VehicleService(CompanyService companyService, VehicleRepository vehicleRepository,
                          VehicleDocumentRepository vehicleDocumentRepository,
                          VehicleMileageReadingRepository vehicleMileageReadingRepository,
                          DocumentService documentService, AccountRepository accountRepository) {
        this.companyService = companyService;
        this.vehicleRepository = vehicleRepository;
        this.vehicleDocumentRepository = vehicleDocumentRepository;
        this.vehicleMileageReadingRepository = vehicleMileageReadingRepository;
        this.documentService = documentService;
        this.accountRepository = accountRepository;
    }

    // ----- Vehicles -----

    @Transactional
    public VehicleResponse createVehicle(UUID currentUserId, UUID companyId, CreateVehicleRequest request) {
        CompanyMember membership = companyService.requirePermission(companyId, currentUserId, Permission.MANAGE_VEHICLES);
        Company company = membership.getCompany();

        String plate = request.registrationPlate().trim();
        if (vehicleRepository.existsByCompanyIdAndRegistrationPlate(companyId, plate)) {
            throw new VehiclePlateTakenException("error.vehicle.plate-taken");
        }

        Vehicle vehicle = new Vehicle();
        vehicle.setCompany(company);
        vehicle.setRegistrationPlate(plate);
        vehicle.setVin(CompanyMapper.blankToNull(request.vin()));
        vehicle.setMake(CompanyMapper.blankToNull(request.make()));
        vehicle.setModel(CompanyMapper.blankToNull(request.model()));
        vehicle.setVersion(CompanyMapper.blankToNull(request.version()));
        vehicle.setVehicleType(request.vehicleType());
        vehicle.setFuelType(request.fuelType());
        vehicle.setFirstRegistrationDate(request.firstRegistrationDate());
        vehicle.setEmissionClass(CompanyMapper.blankToNull(request.emissionClass()));
        vehicle.setGrossWeightKg(request.grossWeightKg());
        vehicle.setPayloadKg(request.payloadKg());
        vehicle.setRegistrationCertificateNumber(CompanyMapper.blankToNull(request.registrationCertificateNumber()));
        vehicle.setInsuranceInfo(VehicleMapper.newInsurance(request.insurance()));
        vehicle.setTechnicalInspectionDate(request.technicalInspectionDate());
        vehicle.setRoadTaxDueDate(request.roadTaxDueDate());
        vehicle.setNotes(CompanyMapper.blankToNull(request.notes()));
        vehicle.setStatus(VehicleStatus.ACTIVE);
        vehicleRepository.save(vehicle);

        return VehicleMapper.toResponse(vehicle, List.of(), companyId);
    }

    @Transactional(readOnly = true)
    public PageResponse<VehicleSummaryResponse> listVehicles(UUID currentUserId, UUID companyId, VehicleStatus status,
                                                             VehicleType type, String q, Pageable pageable) {
        companyService.requireMember(companyId, currentUserId);
        validateSort(pageable.getSort());
        Page<VehicleSummaryResponse> page = vehicleRepository
                .search(companyId, status, type, searchPattern(q), pageable)
                .map(VehicleMapper::toSummary);
        return PageResponse.of(page);
    }

    @Transactional(readOnly = true)
    public VehicleResponse getVehicle(UUID currentUserId, UUID companyId, UUID vehicleId) {
        companyService.requireMember(companyId, currentUserId);
        Vehicle vehicle = loadVehicle(companyId, vehicleId);
        return toFullResponse(vehicle, companyId);
    }

    @Transactional
    public VehicleResponse updateVehicle(UUID currentUserId, UUID companyId, UUID vehicleId,
                                         UpdateVehicleRequest request) {
        companyService.requirePermission(companyId, currentUserId, Permission.MANAGE_VEHICLES);
        Vehicle vehicle = loadVehicle(companyId, vehicleId);

        if (request.registrationPlate() != null) {
            String plate = request.registrationPlate().trim();
            if (!plate.equals(vehicle.getRegistrationPlate())
                    && vehicleRepository.existsByCompanyIdAndRegistrationPlate(companyId, plate)) {
                throw new VehiclePlateTakenException("error.vehicle.plate-taken");
            }
            vehicle.setRegistrationPlate(plate);
        }
        if (request.vin() != null) {
            vehicle.setVin(CompanyMapper.blankToNull(request.vin()));
        }
        if (request.make() != null) {
            vehicle.setMake(CompanyMapper.blankToNull(request.make()));
        }
        if (request.model() != null) {
            vehicle.setModel(CompanyMapper.blankToNull(request.model()));
        }
        if (request.version() != null) {
            vehicle.setVersion(CompanyMapper.blankToNull(request.version()));
        }
        if (request.vehicleType() != null) {
            vehicle.setVehicleType(request.vehicleType());
        }
        if (request.fuelType() != null) {
            vehicle.setFuelType(request.fuelType());
        }
        if (request.firstRegistrationDate() != null) {
            vehicle.setFirstRegistrationDate(request.firstRegistrationDate());
        }
        if (request.emissionClass() != null) {
            vehicle.setEmissionClass(CompanyMapper.blankToNull(request.emissionClass()));
        }
        if (request.grossWeightKg() != null) {
            vehicle.setGrossWeightKg(request.grossWeightKg());
        }
        if (request.payloadKg() != null) {
            vehicle.setPayloadKg(request.payloadKg());
        }
        if (request.registrationCertificateNumber() != null) {
            vehicle.setRegistrationCertificateNumber(CompanyMapper.blankToNull(request.registrationCertificateNumber()));
        }
        if (request.insurance() != null) {
            vehicle.setInsuranceInfo(VehicleMapper.newInsurance(request.insurance()));
        }
        if (request.technicalInspectionDate() != null) {
            vehicle.setTechnicalInspectionDate(request.technicalInspectionDate());
        }
        if (request.roadTaxDueDate() != null) {
            vehicle.setRoadTaxDueDate(request.roadTaxDueDate());
        }
        if (request.notes() != null) {
            vehicle.setNotes(CompanyMapper.blankToNull(request.notes()));
        }

        return toFullResponse(vehicle, companyId);
    }

    @Transactional
    public VehicleResponse setVehicleStatus(UUID currentUserId, UUID companyId, UUID vehicleId, VehicleStatus status) {
        companyService.requirePermission(companyId, currentUserId, Permission.MANAGE_VEHICLES);
        Vehicle vehicle = loadVehicle(companyId, vehicleId);
        vehicle.setStatus(status);
        return toFullResponse(vehicle, companyId);
    }

    @Transactional
    public void deleteVehicle(UUID currentUserId, UUID companyId, UUID vehicleId) {
        companyService.requirePermission(companyId, currentUserId, Permission.MANAGE_VEHICLES);
        Vehicle vehicle = loadVehicle(companyId, vehicleId);

        List<VehicleDocument> documents = vehicleDocumentRepository.findByVehicleIdOrderByCreatedAtDesc(vehicleId);
        for (VehicleDocument vehicleDocument : documents) {
            Document document = vehicleDocument.getDocument();
            vehicleDocumentRepository.delete(vehicleDocument);
            documentService.delete(document);
        }
        for (VehicleMileageReading reading : vehicleMileageReadingRepository
                .findByVehicleIdOrderByRecordedAtDesc(vehicleId, Pageable.unpaged())) {
            vehicleMileageReadingRepository.delete(reading);
        }
        vehicleRepository.delete(vehicle);
    }

    // ----- Documents -----

    @Transactional(readOnly = true)
    public List<VehicleDocumentResponse> listDocuments(UUID currentUserId, UUID companyId, UUID vehicleId,
                                                       VehicleDocumentCategory category) {
        companyService.requireMember(companyId, currentUserId);
        loadVehicle(companyId, vehicleId);
        List<VehicleDocument> documents = category == null
                ? vehicleDocumentRepository.findByVehicleIdOrderByCreatedAtDesc(vehicleId)
                : vehicleDocumentRepository.findByVehicleIdAndCategoryOrderByCreatedAtDesc(vehicleId, category);
        return documents.stream()
                .map(doc -> VehicleDocumentMapper.toResponse(doc, companyId, vehicleId))
                .toList();
    }

    @Transactional
    public VehicleDocumentResponse uploadDocument(UUID currentUserId, UUID companyId, UUID vehicleId,
                                                  VehicleDocumentCategory category, String label, MultipartFile file) {
        companyService.requirePermission(companyId, currentUserId, Permission.MANAGE_VEHICLES);
        Vehicle vehicle = loadVehicle(companyId, vehicleId);

        Document document = documentService.upload(file);
        VehicleDocument vehicleDocument = new VehicleDocument();
        vehicleDocument.setVehicle(vehicle);
        vehicleDocument.setDocument(document);
        vehicleDocument.setCategory(category == null ? VehicleDocumentCategory.OTHER : category);
        vehicleDocument.setLabel(CompanyMapper.blankToNull(label));
        vehicleDocument.setUploadedBy(accountRepository.findById(currentUserId).orElse(null));
        vehicleDocumentRepository.save(vehicleDocument);

        return VehicleDocumentMapper.toResponse(vehicleDocument, companyId, vehicleId);
    }

    @Transactional(readOnly = true)
    public LoadedDocument getDocumentContent(UUID currentUserId, UUID companyId, UUID vehicleId, UUID documentId) {
        companyService.requireMember(companyId, currentUserId);
        loadVehicle(companyId, vehicleId);
        VehicleDocument vehicleDocument = loadVehicleDocument(vehicleId, documentId);
        return documentService.getContent(vehicleDocument.getDocument().getId());
    }

    @Transactional
    public void deleteDocument(UUID currentUserId, UUID companyId, UUID vehicleId, UUID documentId) {
        companyService.requirePermission(companyId, currentUserId, Permission.MANAGE_VEHICLES);
        loadVehicle(companyId, vehicleId);
        VehicleDocument vehicleDocument = loadVehicleDocument(vehicleId, documentId);
        Document document = vehicleDocument.getDocument();
        vehicleDocumentRepository.delete(vehicleDocument);
        documentService.delete(document);
    }

    // ----- Mileage readings -----

    @Transactional
    public MileageReadingResponse addMileageReading(UUID currentUserId, UUID companyId, UUID vehicleId,
                                                    CreateMileageReadingRequest request) {
        companyService.requireMember(companyId, currentUserId);
        Vehicle vehicle = loadVehicle(companyId, vehicleId);

        VehicleMileageReading reading = new VehicleMileageReading();
        reading.setVehicle(vehicle);
        reading.setValueKm(request.valueKm());
        reading.setRecordedAt(request.recordedAt());
        reading.setRecordedBy(accountRepository.findById(currentUserId).orElse(null));
        reading.setNote(CompanyMapper.blankToNull(request.note()));
        vehicleMileageReadingRepository.save(reading);

        if (vehicle.getLatestMileageAt() == null || reading.getRecordedAt().isAfter(vehicle.getLatestMileageAt())) {
            vehicle.setLatestMileageKm(reading.getValueKm());
            vehicle.setLatestMileageAt(reading.getRecordedAt());
        }

        return VehicleMileageReadingMapper.toResponse(reading);
    }

    @Transactional(readOnly = true)
    public PageResponse<MileageReadingResponse> listMileageReadings(UUID currentUserId, UUID companyId, UUID vehicleId,
                                                                   Pageable pageable) {
        companyService.requireMember(companyId, currentUserId);
        loadVehicle(companyId, vehicleId);
        Page<MileageReadingResponse> page = vehicleMileageReadingRepository
                .findByVehicleIdOrderByRecordedAtDesc(vehicleId, pageable)
                .map(VehicleMileageReadingMapper::toResponse);
        return PageResponse.of(page);
    }

    // ----- Helpers -----

    private VehicleResponse toFullResponse(Vehicle vehicle, UUID companyId) {
        List<VehicleDocument> documents = vehicleDocumentRepository.findByVehicleIdOrderByCreatedAtDesc(vehicle.getId());
        return VehicleMapper.toResponse(vehicle, documents, companyId);
    }

    private Vehicle loadVehicle(UUID companyId, UUID vehicleId) {
        return vehicleRepository.findByIdAndCompanyId(vehicleId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("error.vehicle.not-found"));
    }

    private VehicleDocument loadVehicleDocument(UUID vehicleId, UUID documentId) {
        return vehicleDocumentRepository.findByIdAndVehicleId(documentId, vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("error.vehicle.document.not-found"));
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
