package com.lanely.api.service;

import com.lanely.api.dto.waybill.BulkArchiveRequest;
import com.lanely.api.dto.waybill.BulkItemStatus;
import com.lanely.api.dto.waybill.BulkResultResponse;
import com.lanely.api.dto.waybill.BulkStatusRequest;
import com.lanely.api.entity.CompanyMember;
import com.lanely.api.entity.User;
import com.lanely.api.entity.Waybill;
import com.lanely.api.dto.waybill.WaybillDateField;
import com.lanely.api.entity.enums.Permission;
import com.lanely.api.entity.enums.WaybillStatus;
import com.lanely.api.exception.BadRequestException;
import com.lanely.api.exception.MissingPermissionException;
import com.lanely.api.repository.AccountRepository;
import com.lanely.api.repository.ClientAddressRepository;
import com.lanely.api.repository.ClientRepository;
import com.lanely.api.repository.ParcelStatusHistoryRepository;
import com.lanely.api.repository.TourRepository;
import com.lanely.api.repository.WaybillRepository;
import com.lanely.api.repository.WaybillStatusHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WaybillServiceBulkAndFilterTest {

    @Mock private MessageSource messageSource;
    @Mock private CompanyService companyService;
    @Mock private WaybillRepository waybillRepository;
    @Mock private WaybillStatusHistoryRepository waybillStatusHistoryRepository;
    @Mock private ParcelStatusHistoryRepository parcelStatusHistoryRepository;
    @Mock private TourRepository tourRepository;
    @Mock private ClientRepository clientRepository;
    @Mock private ClientAddressRepository clientAddressRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private AssigneeResolver assigneeResolver;
    @Mock private GeocodingService geocodingService;
    @Mock private RoutingService routingService;
    @Mock private ImageService imageService;

    private WaybillService service;

    private final UUID companyId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new WaybillService(messageSource, companyService, waybillRepository, waybillStatusHistoryRepository,
                parcelStatusHistoryRepository, tourRepository, clientRepository, clientAddressRepository,
                accountRepository, assigneeResolver, geocodingService, routingService, imageService);
        when(messageSource.getMessage(anyString(), any(), any())).thenAnswer(inv -> inv.getArgument(0));
        CompanyMember membership = mock(CompanyMember.class);
        when(membership.getUser()).thenReturn(new User());
        when(companyService.requirePermission(eq(companyId), eq(userId), eq(Permission.MANAGE_TRANSPORTS)))
                .thenReturn(membership);
        when(waybillRepository.search(any(), anyBoolean(), any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any())).thenReturn(Page.empty());
    }

    // ----- Assignee list date window -----

    @Test
    void listForAssignee_deliveryWindow_passesDeliveryBoundsOnly() {
        UUID accountId = UUID.randomUUID();
        Instant from = Instant.parse("2026-06-25T00:00:00Z");
        Instant to = Instant.parse("2026-06-26T00:00:00Z");
        when(waybillRepository.findAssignedToAccount(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Page.empty());

        service.listForAssignee(companyId, accountId, null, WaybillDateField.DELIVERY, from, to, PageRequest.of(0, 20));

        org.mockito.Mockito.verify(waybillRepository).findAssignedToAccount(eq(companyId), eq(accountId), eq(null),
                eq(null), eq(null), eq(from), eq(to), eq(null), eq(null), any());
    }

    @Test
    void listForAssignee_invalidRange_throws() {
        UUID accountId = UUID.randomUUID();
        Instant from = Instant.parse("2026-06-26T00:00:00Z");
        Instant to = Instant.parse("2026-06-25T00:00:00Z");

        assertThrows(BadRequestException.class, () -> service.listForAssignee(companyId, accountId, null,
                WaybillDateField.PICKUP, from, to, PageRequest.of(0, 20)));
    }

    // ----- Multi-status & archived filter -----

    @Test
    void list_passesMultipleStatusesAsOrFilter() {
        Pageable pageable = PageRequest.of(0, 20);
        List<WaybillStatus> statuses = List.of(WaybillStatus.ISSUED, WaybillStatus.IN_TRANSIT, WaybillStatus.DELIVERED);

        service.listWaybills(userId, companyId, statuses, null, null, null, null, null, null, null, null, pageable);

        ArgumentCaptor<Collection<WaybillStatus>> statusCaptor = ArgumentCaptor.forClass(Collection.class);
        ArgumentCaptor<Boolean> archivedCaptor = ArgumentCaptor.forClass(Boolean.class);
        org.mockito.Mockito.verify(waybillRepository).search(eq(companyId), eq(true), statusCaptor.capture(),
                archivedCaptor.capture(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
        assertEquals(statuses, List.copyOf(statusCaptor.getValue()));
        assertEquals(Boolean.FALSE, archivedCaptor.getValue());
    }

    @Test
    void list_emptyStatusListMeansNoStatusFilter() {
        service.listWaybills(userId, companyId, List.of(), null, null, null, null, null, null, null, null,
                PageRequest.of(0, 20));

        ArgumentCaptor<Boolean> hasStatusCaptor = ArgumentCaptor.forClass(Boolean.class);
        org.mockito.Mockito.verify(waybillRepository).search(eq(companyId), hasStatusCaptor.capture(),
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
        assertEquals(Boolean.FALSE, hasStatusCaptor.getValue());
    }

    @Test
    void list_archivedAllMeansNoArchivedFilter() {
        service.listWaybills(userId, companyId, null, "all", null, null, null, null, null, null, null,
                PageRequest.of(0, 20));

        ArgumentCaptor<Boolean> archivedCaptor = ArgumentCaptor.forClass(Boolean.class);
        org.mockito.Mockito.verify(waybillRepository).search(eq(companyId), anyBoolean(), any(),
                archivedCaptor.capture(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
        assertNull(archivedCaptor.getValue());
    }

    @Test
    void list_archivedTrueFiltersArchivedOnly() {
        service.listWaybills(userId, companyId, null, "true", null, null, null, null, null, null, null,
                PageRequest.of(0, 20));

        ArgumentCaptor<Boolean> archivedCaptor = ArgumentCaptor.forClass(Boolean.class);
        org.mockito.Mockito.verify(waybillRepository).search(eq(companyId), anyBoolean(), any(),
                archivedCaptor.capture(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
        assertEquals(Boolean.TRUE, archivedCaptor.getValue());
    }

    @Test
    void list_invalidArchivedValueIsRejected() {
        assertThrows(BadRequestException.class, () ->
                service.listWaybills(userId, companyId, null, "maybe", null, null, null, null, null, null, null,
                        PageRequest.of(0, 20)));
    }

    // ----- Bulk status: partial results, transitions, scoping -----

    @Test
    void bulkChangeStatus_appliesPartialResultsAndScopesToCompany() {
        UUID present = UUID.randomUUID();
        UUID missing = UUID.randomUUID();
        Waybill waybill = waybill(WaybillStatus.ISSUED);
        when(waybillRepository.findByIdAndCompanyId(present, companyId)).thenReturn(Optional.of(waybill));
        when(waybillRepository.findByIdAndCompanyId(missing, companyId)).thenReturn(Optional.empty());

        BulkResultResponse result = service.bulkChangeStatus(userId, companyId,
                new BulkStatusRequest(List.of(present, missing), WaybillStatus.IN_TRANSIT, null, null));

        assertEquals(1, result.succeeded());
        assertEquals(1, result.failed());
        assertEquals(WaybillStatus.IN_TRANSIT, waybill.getStatus());
        assertEquals(BulkItemStatus.OK, byId(result, present).status());
        assertEquals(BulkItemStatus.ERROR, byId(result, missing).status());
        assertEquals("WAYBILL_NOT_FOUND", byId(result, missing).error().code());
    }

    @Test
    void bulkChangeStatus_failedWithoutReasonIsPerItemError() {
        UUID id = UUID.randomUUID();
        Waybill waybill = waybill(WaybillStatus.IN_TRANSIT);
        when(waybillRepository.findByIdAndCompanyId(id, companyId)).thenReturn(Optional.of(waybill));

        BulkResultResponse result = service.bulkChangeStatus(userId, companyId,
                new BulkStatusRequest(List.of(id), WaybillStatus.FAILED, null, null));

        assertEquals(0, result.succeeded());
        assertEquals(1, result.failed());
        assertEquals("FAILURE_REASON_REQUIRED", byId(result, id).error().code());
        assertEquals(WaybillStatus.IN_TRANSIT, waybill.getStatus());
    }

    @Test
    void bulkChangeStatus_requiresManageTransportsPermission() {
        when(companyService.requirePermission(eq(companyId), eq(userId), eq(Permission.MANAGE_TRANSPORTS)))
                .thenThrow(new MissingPermissionException("error.permission.missing", "MANAGE_TRANSPORTS"));

        assertThrows(MissingPermissionException.class, () -> service.bulkChangeStatus(userId, companyId,
                new BulkStatusRequest(List.of(UUID.randomUUID()), WaybillStatus.IN_TRANSIT, null, null)));
    }

    @Test
    void bulkChangeStatus_rejectsEmptyBatch() {
        assertThrows(BadRequestException.class, () -> service.bulkChangeStatus(userId, companyId,
                new BulkStatusRequest(List.of(), WaybillStatus.IN_TRANSIT, null, null)));
    }

    @Test
    void bulkChangeStatus_rejectsTooManyIds() {
        List<UUID> ids = java.util.stream.Stream.generate(UUID::randomUUID)
                .limit(WaybillService.MAX_BULK_IDS + 1).toList();
        assertThrows(BadRequestException.class, () -> service.bulkChangeStatus(userId, companyId,
                new BulkStatusRequest(ids, WaybillStatus.IN_TRANSIT, null, null)));
    }

    // ----- Bulk archive -----

    @Test
    void bulkArchive_archivesPresentAndReportsMissing() {
        UUID present = UUID.randomUUID();
        UUID missing = UUID.randomUUID();
        Waybill waybill = waybill(WaybillStatus.DELIVERED);
        when(waybillRepository.findByIdAndCompanyId(present, companyId)).thenReturn(Optional.of(waybill));
        when(waybillRepository.findByIdAndCompanyId(missing, companyId)).thenReturn(Optional.empty());

        BulkResultResponse result = service.bulkArchive(userId, companyId,
                new BulkArchiveRequest(List.of(present, missing), true));

        assertEquals(1, result.succeeded());
        assertEquals(1, result.failed());
        assertTrue(waybill.isArchived());
        assertNull(byId(result, present).error());
        assertEquals("WAYBILL_NOT_FOUND", byId(result, missing).error().code());
    }

    private static com.lanely.api.dto.waybill.BulkItemResult byId(BulkResultResponse result, UUID id) {
        return result.results().stream().filter(r -> r.id().equals(id)).findFirst().orElseThrow();
    }

    private static Waybill waybill(WaybillStatus status) {
        Waybill waybill = new Waybill();
        waybill.setStatus(status);
        return waybill;
    }
}
