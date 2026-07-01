package com.lanely.api.mapper;

import com.lanely.api.dto.waybill.WaybillSummaryResponse;
import com.lanely.api.entity.Client;
import com.lanely.api.entity.Waybill;
import com.lanely.api.entity.WaybillParty;
import com.lanely.api.entity.embeddable.Address;
import com.lanely.api.entity.embeddable.GeoPoint;
import com.lanely.api.entity.enums.WaybillPartyRole;
import com.lanely.api.entity.enums.WaybillScope;
import com.lanely.api.entity.enums.WaybillStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class WaybillMapperTest {

    @Test
    void toSummary_mapsPickupAndDeliverySnapshot() {
        Client client = new Client();
        client.setName("ACME Retail");

        Waybill waybill = new Waybill();
        waybill.setClient(client);
        waybill.setReference("WBL-0001");
        waybill.setStatus(WaybillStatus.ISSUED);
        waybill.setScope(WaybillScope.NATIONAL);

        Address pickupAddress = new Address();
        pickupAddress.setCity("Paris");
        waybill.setPlaceOfTakingOver(pickupAddress);
        waybill.setTakingOverLocation(new GeoPoint(48.8566, 2.3522));
        Instant pickupAt = Instant.parse("2026-06-25T08:00:00Z");
        waybill.setTakingOverPlannedAt(pickupAt);

        Address deliveryAddress = new Address();
        deliveryAddress.setCity("Rennes");
        waybill.setPlaceOfDelivery(deliveryAddress);
        waybill.setDeliveryLocation(new GeoPoint(48.1173, -1.6778));
        Instant deliveryAt = Instant.parse("2026-06-25T16:00:00Z");
        waybill.setDeliveryPlannedAt(deliveryAt);

        waybill.getParties().add(party(waybill, WaybillPartyRole.SHIPPER, "ACME Logistics"));
        waybill.getParties().add(party(waybill, WaybillPartyRole.CONSIGNEE, "Jean Martin"));

        WaybillSummaryResponse summary = WaybillMapper.toSummary(waybill);

        assertEquals("WBL-0001", summary.reference());
        assertEquals("Paris", summary.pickupCity());
        assertEquals(48.8566, summary.pickupLatitude());
        assertEquals(2.3522, summary.pickupLongitude());
        assertEquals(pickupAt, summary.pickupPlannedAt());
        assertEquals("Rennes", summary.deliveryCity());
        assertEquals(48.1173, summary.deliveryLatitude());
        assertEquals(-1.6778, summary.deliveryLongitude());
        assertEquals(deliveryAt, summary.deliveryPlannedAt());
    }

    @Test
    void toSummary_returnsNullPickupFieldsWhenTakingOverMissing() {
        Client client = new Client();
        client.setName("ACME Retail");

        Waybill waybill = new Waybill();
        waybill.setClient(client);
        waybill.setReference("WBL-0002");
        waybill.setStatus(WaybillStatus.DRAFT);
        waybill.setScope(WaybillScope.NATIONAL);
        waybill.setPlaceOfTakingOver(null);
        waybill.setTakingOverLocation(null);
        waybill.setTakingOverPlannedAt(null);

        WaybillSummaryResponse summary = WaybillMapper.toSummary(waybill);

        assertEquals("WBL-0002", summary.reference());
        assertNull(summary.pickupCity());
        assertNull(summary.pickupLatitude());
        assertNull(summary.pickupLongitude());
        assertNull(summary.pickupPlannedAt());
    }

    private static WaybillParty party(Waybill waybill, WaybillPartyRole role, String name) {
        WaybillParty party = new WaybillParty();
        party.setWaybill(waybill);
        party.setRole(role);
        party.setName(name);
        return party;
    }
}
