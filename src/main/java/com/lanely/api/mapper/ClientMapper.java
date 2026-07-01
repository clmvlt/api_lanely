package com.lanely.api.mapper;

import com.lanely.api.dto.client.ClientResponse;
import com.lanely.api.dto.client.ClientSettingsDto;
import com.lanely.api.dto.client.ClientSummaryResponse;
import com.lanely.api.entity.Client;
import com.lanely.api.entity.ClientAddress;
import com.lanely.api.entity.ClientContact;
import com.lanely.api.entity.CompanyMember;
import com.lanely.api.entity.embeddable.ClientSettings;

import java.util.List;

public final class ClientMapper {

    private ClientMapper() {
    }

    public static ClientSummaryResponse toSummary(Client client) {
        return new ClientSummaryResponse(client.getId(), client.getReference(), client.getType(), client.getName(),
                client.getEmail(), client.getPhone(), client.getStatus(), client.isDeliveryBlocked(),
                client.getCreatedAt());
    }

    public static ClientResponse toResponse(Client client, List<ClientAddress> addresses, List<ClientContact> contacts) {
        return new ClientResponse(client.getId(), client.getReference(), client.getType(), client.getName(),
                CompanyMapper.toLegalInfoDto(client.getLegalInfo()), client.getEmail(), client.getPhone(),
                client.getWebsite(), client.getPaymentTermsDays(), client.getStatus(), client.isDeliveryBlocked(),
                accountManagerUserId(client), client.getNotes(), toSettingsDto(client.getSettings()),
                addresses.stream().map(ClientAddressMapper::toResponse).toList(),
                contacts.stream().map(ClientContactMapper::toResponse).toList(),
                client.getCreatedAt(), client.getUpdatedAt());
    }

    public static ClientSettingsDto toSettingsDto(ClientSettings settings) {
        if (settings == null) {
            return null;
        }
        return new ClientSettingsDto(settings.getPreferredLanguage(), settings.isAutoSendInvoiceEmail(),
                settings.isAutoSendDeliveryNotifications(), settings.isAutoSendPaymentReminders(),
                settings.getBillingEmail());
    }

    public static ClientSettings newSettings(ClientSettingsDto dto) {
        ClientSettings settings = new ClientSettings();
        applySettings(settings, dto);
        return settings;
    }

    public static void applySettings(ClientSettings settings, ClientSettingsDto dto) {
        if (dto == null) {
            return;
        }
        if (dto.preferredLanguage() != null) {
            settings.setPreferredLanguage(dto.preferredLanguage());
        }
        if (dto.autoSendInvoiceEmail() != null) {
            settings.setAutoSendInvoiceEmail(dto.autoSendInvoiceEmail());
        }
        if (dto.autoSendDeliveryNotifications() != null) {
            settings.setAutoSendDeliveryNotifications(dto.autoSendDeliveryNotifications());
        }
        if (dto.autoSendPaymentReminders() != null) {
            settings.setAutoSendPaymentReminders(dto.autoSendPaymentReminders());
        }
        if (dto.billingEmail() != null) {
            settings.setBillingEmail(CompanyMapper.blankToNull(dto.billingEmail()));
        }
    }

    private static java.util.UUID accountManagerUserId(Client client) {
        CompanyMember manager = client.getAccountManager();
        return manager == null ? null : manager.getUser().getId();
    }
}
