package com.lanely.api.entity.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class ClientSettings {

    @Column(name = "settings_language", length = 8)
    private String preferredLanguage = "en";

    @Column(name = "settings_auto_send_invoice_email", nullable = false)
    private boolean autoSendInvoiceEmail = false;

    @Column(name = "settings_auto_send_delivery_notifications", nullable = false)
    private boolean autoSendDeliveryNotifications = false;

    @Column(name = "settings_auto_send_payment_reminders", nullable = false)
    private boolean autoSendPaymentReminders = false;

    @Column(name = "settings_billing_email", length = 255)
    private String billingEmail;

    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    public void setPreferredLanguage(String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    public boolean isAutoSendInvoiceEmail() {
        return autoSendInvoiceEmail;
    }

    public void setAutoSendInvoiceEmail(boolean autoSendInvoiceEmail) {
        this.autoSendInvoiceEmail = autoSendInvoiceEmail;
    }

    public boolean isAutoSendDeliveryNotifications() {
        return autoSendDeliveryNotifications;
    }

    public void setAutoSendDeliveryNotifications(boolean autoSendDeliveryNotifications) {
        this.autoSendDeliveryNotifications = autoSendDeliveryNotifications;
    }

    public boolean isAutoSendPaymentReminders() {
        return autoSendPaymentReminders;
    }

    public void setAutoSendPaymentReminders(boolean autoSendPaymentReminders) {
        this.autoSendPaymentReminders = autoSendPaymentReminders;
    }

    public String getBillingEmail() {
        return billingEmail;
    }

    public void setBillingEmail(String billingEmail) {
        this.billingEmail = billingEmail;
    }
}
