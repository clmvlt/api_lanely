package com.lanely.api.service;

import com.lanely.api.dto.client.ClientAddressResponse;
import com.lanely.api.dto.client.ClientContactResponse;
import com.lanely.api.dto.client.ClientResponse;
import com.lanely.api.dto.client.ClientSummaryResponse;
import com.lanely.api.dto.client.CreateClientAddressRequest;
import com.lanely.api.dto.client.CreateClientContactRequest;
import com.lanely.api.dto.client.CreateClientRequest;
import com.lanely.api.dto.client.UpdateClientAddressRequest;
import com.lanely.api.dto.client.UpdateClientContactRequest;
import com.lanely.api.dto.client.UpdateClientRequest;
import com.lanely.api.dto.common.PageResponse;
import com.lanely.api.entity.Client;
import com.lanely.api.entity.ClientAddress;
import com.lanely.api.entity.ClientContact;
import com.lanely.api.entity.Company;
import com.lanely.api.entity.CompanyMember;
import com.lanely.api.entity.embeddable.Address;
import com.lanely.api.entity.enums.AddressType;
import com.lanely.api.entity.enums.ClientStatus;
import com.lanely.api.exception.BadRequestException;
import com.lanely.api.exception.ClientReferenceTakenException;
import com.lanely.api.exception.ResourceNotFoundException;
import com.lanely.api.entity.enums.Permission;
import com.lanely.api.mapper.ClientAddressMapper;
import com.lanely.api.mapper.ClientContactMapper;
import com.lanely.api.mapper.ClientMapper;
import com.lanely.api.mapper.CompanyMapper;
import com.lanely.api.repository.ClientAddressRepository;
import com.lanely.api.repository.ClientContactRepository;
import com.lanely.api.repository.ClientRepository;
import com.lanely.api.repository.CompanyMemberRepository;
import com.lanely.api.repository.WaybillPartyRepository;
import com.lanely.api.repository.WaybillRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class ClientService {

    private static final Set<String> SORTABLE_FIELDS = Set.of("name", "reference", "createdAt", "updatedAt", "status");

    private final CompanyService companyService;
    private final ClientRepository clientRepository;
    private final ClientAddressRepository clientAddressRepository;
    private final ClientContactRepository clientContactRepository;
    private final CompanyMemberRepository companyMemberRepository;
    private final WaybillRepository waybillRepository;
    private final WaybillPartyRepository waybillPartyRepository;
    private final AddressGeocodingService addressGeocodingService;

    public ClientService(CompanyService companyService, ClientRepository clientRepository,
                         ClientAddressRepository clientAddressRepository,
                         ClientContactRepository clientContactRepository,
                         CompanyMemberRepository companyMemberRepository,
                         WaybillRepository waybillRepository,
                         WaybillPartyRepository waybillPartyRepository,
                         AddressGeocodingService addressGeocodingService) {
        this.companyService = companyService;
        this.clientRepository = clientRepository;
        this.clientAddressRepository = clientAddressRepository;
        this.clientContactRepository = clientContactRepository;
        this.companyMemberRepository = companyMemberRepository;
        this.waybillRepository = waybillRepository;
        this.waybillPartyRepository = waybillPartyRepository;
        this.addressGeocodingService = addressGeocodingService;
    }

    // ----- Clients -----

    @Transactional
    public ClientResponse createClient(UUID currentUserId, UUID companyId, CreateClientRequest request) {
        CompanyMember membership = companyService.requirePermission(companyId, currentUserId, Permission.MANAGE_CLIENTS);
        Company company = membership.getCompany();

        Client client = new Client();
        client.setCompany(company);
        client.setReference(resolveNewReference(companyId, request.reference()));
        client.setType(request.type());
        client.setName(request.name().trim());
        client.setLegalInfo(CompanyMapper.toLegalInfo(request.legalInfo()));
        client.setEmail(CompanyMapper.blankToNull(request.email()));
        client.setPhone(CompanyMapper.blankToNull(request.phone()));
        client.setWebsite(CompanyMapper.blankToNull(request.website()));
        client.setPaymentTermsDays(request.paymentTermsDays() == null ? 30 : request.paymentTermsDays());
        client.setAccountManager(resolveAccountManager(companyId, request.accountManagerUserId()));
        client.setNotes(CompanyMapper.blankToNull(request.notes()));
        client.setSettings(ClientMapper.newSettings(request.settings()));
        client.setStatus(ClientStatus.ACTIVE);
        clientRepository.save(client);

        return ClientMapper.toResponse(client, List.of(), List.of());
    }

    @Transactional(readOnly = true)
    public PageResponse<ClientSummaryResponse> listClients(UUID currentUserId, UUID companyId, ClientStatus status,
                                                          String q, Pageable pageable) {
        companyService.requireMember(companyId, currentUserId);
        validateSort(pageable.getSort());
        Page<ClientSummaryResponse> page = clientRepository
                .search(companyId, status, searchPattern(q), pageable)
                .map(ClientMapper::toSummary);
        return PageResponse.of(page);
    }

    @Transactional(readOnly = true)
    public ClientResponse getClient(UUID currentUserId, UUID companyId, UUID clientId) {
        companyService.requireMember(companyId, currentUserId);
        Client client = loadClient(companyId, clientId);
        return toFullResponse(client);
    }

    @Transactional
    public ClientResponse updateClient(UUID currentUserId, UUID companyId, UUID clientId, UpdateClientRequest request) {
        companyService.requirePermission(companyId, currentUserId, Permission.MANAGE_CLIENTS);
        Client client = loadClient(companyId, clientId);

        if (request.reference() != null) {
            String reference = request.reference().trim();
            if (!reference.equals(client.getReference())
                    && clientRepository.existsByCompanyIdAndReference(companyId, reference)) {
                throw new ClientReferenceTakenException("error.client.reference-taken");
            }
            client.setReference(reference);
        }
        if (request.type() != null) {
            client.setType(request.type());
        }
        if (request.name() != null) {
            client.setName(request.name().trim());
        }
        if (request.legalInfo() != null) {
            client.setLegalInfo(CompanyMapper.toLegalInfo(request.legalInfo()));
        }
        if (request.email() != null) {
            client.setEmail(CompanyMapper.blankToNull(request.email()));
        }
        if (request.phone() != null) {
            client.setPhone(CompanyMapper.blankToNull(request.phone()));
        }
        if (request.website() != null) {
            client.setWebsite(CompanyMapper.blankToNull(request.website()));
        }
        if (request.paymentTermsDays() != null) {
            client.setPaymentTermsDays(request.paymentTermsDays());
        }
        if (request.deliveryBlocked() != null) {
            client.setDeliveryBlocked(request.deliveryBlocked());
        }
        if (request.accountManagerUserId() != null) {
            client.setAccountManager(resolveAccountManager(companyId, request.accountManagerUserId()));
        }
        if (request.notes() != null) {
            client.setNotes(CompanyMapper.blankToNull(request.notes()));
        }
        ClientMapper.applySettings(client.getSettings(), request.settings());

        return toFullResponse(client);
    }

    @Transactional
    public ClientResponse setClientStatus(UUID currentUserId, UUID companyId, UUID clientId, ClientStatus status) {
        companyService.requirePermission(companyId, currentUserId, Permission.MANAGE_CLIENTS);
        Client client = loadClient(companyId, clientId);
        client.setStatus(status);
        return toFullResponse(client);
    }

    @Transactional
    public void deleteArchivedClient(UUID currentUserId, UUID companyId, UUID clientId) {
        companyService.requirePermission(companyId, currentUserId, Permission.MANAGE_CLIENTS);
        Client client = loadClient(companyId, clientId);
        if (client.getStatus() != ClientStatus.ARCHIVED) {
            throw new BadRequestException("error.client.delete.not-archived");
        }

        List<UUID> addressIds = clientAddressRepository.findByClientIdOrderByCreatedAtAsc(clientId).stream()
                .map(ClientAddress::getId)
                .toList();

        waybillRepository.detachClient(clientId);
        waybillRepository.clearPickupClient(clientId);
        waybillRepository.clearDeliveryClient(clientId);
        waybillPartyRepository.clearClient(clientId);
        if (!addressIds.isEmpty()) {
            waybillRepository.clearPickupClientAddresses(addressIds);
            waybillRepository.clearDeliveryClientAddresses(addressIds);
            waybillPartyRepository.clearClientAddresses(addressIds);
        }

        clientContactRepository.deleteByClientId(clientId);
        clientAddressRepository.deleteByClientId(clientId);
        clientRepository.delete(client);
    }

    // ----- Addresses -----

    @Transactional(readOnly = true)
    public List<ClientAddressResponse> listAddresses(UUID currentUserId, UUID companyId, UUID clientId) {
        companyService.requireMember(companyId, currentUserId);
        loadClient(companyId, clientId);
        return clientAddressRepository.findByClientIdOrderByCreatedAtAsc(clientId).stream()
                .map(ClientAddressMapper::toResponse)
                .toList();
    }

    @Transactional
    public ClientAddressResponse addAddress(UUID currentUserId, UUID companyId, UUID clientId,
                                           CreateClientAddressRequest request) {
        CompanyMember membership = companyService.requirePermission(companyId, currentUserId, Permission.MANAGE_CLIENTS);
        Client client = loadClient(companyId, clientId);

        ClientAddress address = new ClientAddress();
        address.setClient(client);
        address.setLabel(CompanyMapper.blankToNull(request.label()));
        address.setType(request.type() == null ? AddressType.DEPOT : request.type());
        address.setAddress(ClientAddressMapper.toAddress(request.address(), companyCountry(membership.getCompany())));
        address.setLatitude(request.latitude());
        address.setLongitude(request.longitude());
        if (request.latitude() == null || request.longitude() == null) {
            applyAutoCoordinates(address);
        }
        address.setPrimary(orFalse(request.isPrimary()));
        address.setDefaultBilling(orFalse(request.isDefaultBilling()));
        address.setDefaultShipping(orFalse(request.isDefaultShipping()));
        address.setContactName(CompanyMapper.blankToNull(request.contactName()));
        address.setContactPhone(CompanyMapper.blankToNull(request.contactPhone()));
        address.setContactEmail(CompanyMapper.blankToNull(request.contactEmail()));
        address.setDeliveryInstructions(CompanyMapper.blankToNull(request.deliveryInstructions()));
        clientAddressRepository.save(address);

        enforceSingleAddressFlags(address);
        return ClientAddressMapper.toResponse(address);
    }

    @Transactional
    public ClientAddressResponse updateAddress(UUID currentUserId, UUID companyId, UUID clientId, UUID addressId,
                                              UpdateClientAddressRequest request) {
        CompanyMember membership = companyService.requirePermission(companyId, currentUserId, Permission.MANAGE_CLIENTS);
        loadClient(companyId, clientId);
        ClientAddress address = loadAddress(clientId, addressId);

        if (request.label() != null) {
            address.setLabel(CompanyMapper.blankToNull(request.label()));
        }
        if (request.type() != null) {
            address.setType(request.type());
        }
        boolean addressChanged = false;
        if (request.address() != null) {
            address.setAddress(ClientAddressMapper.toAddress(request.address(), companyCountry(membership.getCompany())));
            addressChanged = true;
        }
        if (request.latitude() != null) {
            address.setLatitude(request.latitude());
        }
        if (request.longitude() != null) {
            address.setLongitude(request.longitude());
        }
        if (addressChanged && request.latitude() == null && request.longitude() == null) {
            applyAutoCoordinates(address);
        }
        if (request.isPrimary() != null) {
            address.setPrimary(request.isPrimary());
        }
        if (request.isDefaultBilling() != null) {
            address.setDefaultBilling(request.isDefaultBilling());
        }
        if (request.isDefaultShipping() != null) {
            address.setDefaultShipping(request.isDefaultShipping());
        }
        if (request.contactName() != null) {
            address.setContactName(CompanyMapper.blankToNull(request.contactName()));
        }
        if (request.contactPhone() != null) {
            address.setContactPhone(CompanyMapper.blankToNull(request.contactPhone()));
        }
        if (request.contactEmail() != null) {
            address.setContactEmail(CompanyMapper.blankToNull(request.contactEmail()));
        }
        if (request.deliveryInstructions() != null) {
            address.setDeliveryInstructions(CompanyMapper.blankToNull(request.deliveryInstructions()));
        }
        if (request.active() != null) {
            address.setActive(request.active());
        }

        enforceSingleAddressFlags(address);
        return ClientAddressMapper.toResponse(address);
    }

    @Transactional
    public void deleteAddress(UUID currentUserId, UUID companyId, UUID clientId, UUID addressId) {
        companyService.requirePermission(companyId, currentUserId, Permission.MANAGE_CLIENTS);
        loadClient(companyId, clientId);
        ClientAddress address = loadAddress(clientId, addressId);
        clientAddressRepository.delete(address);
    }

    // ----- Contacts -----

    @Transactional(readOnly = true)
    public List<ClientContactResponse> listContacts(UUID currentUserId, UUID companyId, UUID clientId) {
        companyService.requireMember(companyId, currentUserId);
        loadClient(companyId, clientId);
        return clientContactRepository.findByClientIdOrderByCreatedAtAsc(clientId).stream()
                .map(ClientContactMapper::toResponse)
                .toList();
    }

    @Transactional
    public ClientContactResponse addContact(UUID currentUserId, UUID companyId, UUID clientId,
                                           CreateClientContactRequest request) {
        companyService.requirePermission(companyId, currentUserId, Permission.MANAGE_CLIENTS);
        Client client = loadClient(companyId, clientId);

        ClientContact contact = new ClientContact();
        contact.setClient(client);
        contact.setFirstName(CompanyMapper.blankToNull(request.firstName()));
        contact.setLastName(CompanyMapper.blankToNull(request.lastName()));
        contact.setJobTitle(CompanyMapper.blankToNull(request.jobTitle()));
        contact.setEmail(CompanyMapper.blankToNull(request.email()));
        contact.setPhone(CompanyMapper.blankToNull(request.phone()));
        contact.setPrimary(orFalse(request.isPrimary()));
        contact.setReceivesInvoices(orFalse(request.receivesInvoices()));
        contact.setReceivesDeliveryNotifications(orFalse(request.receivesDeliveryNotifications()));
        clientContactRepository.save(contact);

        enforceSinglePrimaryContact(contact);
        return ClientContactMapper.toResponse(contact);
    }

    @Transactional
    public ClientContactResponse updateContact(UUID currentUserId, UUID companyId, UUID clientId, UUID contactId,
                                              UpdateClientContactRequest request) {
        companyService.requirePermission(companyId, currentUserId, Permission.MANAGE_CLIENTS);
        loadClient(companyId, clientId);
        ClientContact contact = loadContact(clientId, contactId);

        if (request.firstName() != null) {
            contact.setFirstName(CompanyMapper.blankToNull(request.firstName()));
        }
        if (request.lastName() != null) {
            contact.setLastName(CompanyMapper.blankToNull(request.lastName()));
        }
        if (request.jobTitle() != null) {
            contact.setJobTitle(CompanyMapper.blankToNull(request.jobTitle()));
        }
        if (request.email() != null) {
            contact.setEmail(CompanyMapper.blankToNull(request.email()));
        }
        if (request.phone() != null) {
            contact.setPhone(CompanyMapper.blankToNull(request.phone()));
        }
        if (request.isPrimary() != null) {
            contact.setPrimary(request.isPrimary());
        }
        if (request.receivesInvoices() != null) {
            contact.setReceivesInvoices(request.receivesInvoices());
        }
        if (request.receivesDeliveryNotifications() != null) {
            contact.setReceivesDeliveryNotifications(request.receivesDeliveryNotifications());
        }
        if (request.active() != null) {
            contact.setActive(request.active());
        }

        enforceSinglePrimaryContact(contact);
        return ClientContactMapper.toResponse(contact);
    }

    @Transactional
    public void deleteContact(UUID currentUserId, UUID companyId, UUID clientId, UUID contactId) {
        companyService.requirePermission(companyId, currentUserId, Permission.MANAGE_CLIENTS);
        loadClient(companyId, clientId);
        ClientContact contact = loadContact(clientId, contactId);
        clientContactRepository.delete(contact);
    }

    // ----- Helpers -----

    private ClientResponse toFullResponse(Client client) {
        List<ClientAddress> addresses = clientAddressRepository.findByClientIdOrderByCreatedAtAsc(client.getId());
        List<ClientContact> contacts = clientContactRepository.findByClientIdOrderByCreatedAtAsc(client.getId());
        return ClientMapper.toResponse(client, addresses, contacts);
    }

    private Client loadClient(UUID companyId, UUID clientId) {
        return clientRepository.findByIdAndCompanyId(clientId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("error.client.not-found"));
    }

    private ClientAddress loadAddress(UUID clientId, UUID addressId) {
        return clientAddressRepository.findByIdAndClientId(addressId, clientId)
                .orElseThrow(() -> new ResourceNotFoundException("error.client.address.not-found"));
    }

    private ClientContact loadContact(UUID clientId, UUID contactId) {
        return clientContactRepository.findByIdAndClientId(contactId, clientId)
                .orElseThrow(() -> new ResourceNotFoundException("error.client.contact.not-found"));
    }

    private CompanyMember resolveAccountManager(UUID companyId, UUID userId) {
        if (userId == null) {
            return null;
        }
        return companyMemberRepository.findByCompanyIdAndUserId(companyId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("error.member.not-found"));
    }

    private String resolveNewReference(UUID companyId, String requested) {
        String reference = CompanyMapper.blankToNull(requested);
        if (reference != null) {
            if (clientRepository.existsByCompanyIdAndReference(companyId, reference)) {
                throw new ClientReferenceTakenException("error.client.reference-taken");
            }
            return reference;
        }
        long next = clientRepository.countByCompanyId(companyId) + 1;
        String generated;
        do {
            generated = String.format("CLI-%04d", next++);
        } while (clientRepository.existsByCompanyIdAndReference(companyId, generated));
        return generated;
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

    private void applyAutoCoordinates(ClientAddress address) {
        addressGeocodingService.resolve(address.getAddress()).ifPresent(result -> {
            address.setLatitude(result.lat());
            address.setLongitude(result.lon());
        });
    }

    private String companyCountry(Company company) {
        Address billing = company.getBillingAddress();
        return billing == null ? null : billing.getCountry();
    }

    private void enforceSingleAddressFlags(ClientAddress saved) {
        if (!saved.isPrimary() && !saved.isDefaultBilling() && !saved.isDefaultShipping()) {
            return;
        }
        for (ClientAddress sibling : clientAddressRepository.findByClientIdOrderByCreatedAtAsc(saved.getClient().getId())) {
            if (sibling.getId().equals(saved.getId())) {
                continue;
            }
            if (saved.isPrimary() && sibling.isPrimary()) {
                sibling.setPrimary(false);
            }
            if (saved.isDefaultBilling() && sibling.isDefaultBilling()) {
                sibling.setDefaultBilling(false);
            }
            if (saved.isDefaultShipping() && sibling.isDefaultShipping()) {
                sibling.setDefaultShipping(false);
            }
        }
    }

    private void enforceSinglePrimaryContact(ClientContact saved) {
        if (!saved.isPrimary()) {
            return;
        }
        for (ClientContact sibling : clientContactRepository.findByClientIdOrderByCreatedAtAsc(saved.getClient().getId())) {
            if (!sibling.getId().equals(saved.getId()) && sibling.isPrimary()) {
                sibling.setPrimary(false);
            }
        }
    }

    private boolean orFalse(Boolean value) {
        return value != null && value;
    }
}
