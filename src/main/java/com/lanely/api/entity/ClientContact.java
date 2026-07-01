package com.lanely.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "client_contacts", indexes = @Index(name = "idx_client_contact_client", columnList = "client_id"))
public class ClientContact extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(name = "job_title", length = 120)
    private String jobTitle;

    @Column(length = 255)
    private String email;

    @Column(length = 32)
    private String phone;

    @Column(name = "is_primary", nullable = false)
    private boolean primary = false;

    @Column(name = "receives_invoices", nullable = false)
    private boolean receivesInvoices = false;

    @Column(name = "receives_delivery_notifications", nullable = false)
    private boolean receivesDeliveryNotifications = false;

    @Column(nullable = false)
    private boolean active = true;

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isPrimary() {
        return primary;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    public boolean isReceivesInvoices() {
        return receivesInvoices;
    }

    public void setReceivesInvoices(boolean receivesInvoices) {
        this.receivesInvoices = receivesInvoices;
    }

    public boolean isReceivesDeliveryNotifications() {
        return receivesDeliveryNotifications;
    }

    public void setReceivesDeliveryNotifications(boolean receivesDeliveryNotifications) {
        this.receivesDeliveryNotifications = receivesDeliveryNotifications;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
