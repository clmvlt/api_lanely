package com.lanely.api.mapper;

import com.lanely.api.dto.client.ClientContactResponse;
import com.lanely.api.entity.ClientContact;

public final class ClientContactMapper {

    private ClientContactMapper() {
    }

    public static ClientContactResponse toResponse(ClientContact contact) {
        return new ClientContactResponse(contact.getId(), contact.getFirstName(), contact.getLastName(),
                contact.getJobTitle(), contact.getEmail(), contact.getPhone(), contact.isPrimary(),
                contact.isReceivesInvoices(), contact.isReceivesDeliveryNotifications(), contact.isActive(),
                contact.getCreatedAt(), contact.getUpdatedAt());
    }
}
