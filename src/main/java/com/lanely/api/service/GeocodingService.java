package com.lanely.api.service;

import com.lanely.api.entity.embeddable.Address;
import com.lanely.api.entity.embeddable.GeoPoint;
import com.lanely.api.exception.BadRequestException;
import com.lanely.api.integration.ors.GeocodingClient;
import com.lanely.api.integration.ors.dto.OrsGeocodeResult;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GeocodingService {

    private final GeocodingClient geocodingClient;

    public GeocodingService(GeocodingClient geocodingClient) {
        this.geocodingClient = geocodingClient;
    }

    /**
     * Best-effort server-side geocoding used as a fallback when the frontend did not supply coordinates.
     * Returns an empty (incomplete) {@link GeoPoint} when nothing can be resolved.
     */
    public GeoPoint geocodeBest(Address address) {
        String query = toQuery(address);
        if (query == null) {
            return new GeoPoint();
        }
        List<OrsGeocodeResult> results = geocodingClient.search(query, 1, null, null);
        if (results.isEmpty()) {
            return new GeoPoint();
        }
        OrsGeocodeResult best = results.get(0);
        return new GeoPoint(best.lat(), best.lon());
    }

    /**
     * Mandatory server-side geocoding: resolves the address to coordinates and fails when it cannot be located.
     * Throws {@code error.geocoding.address-not-found} (400) when the address is empty or yields no result, and
     * propagates the geocoding subsystem unavailability (503).
     */
    public GeoPoint geocodeRequired(Address address) {
        String query = toQuery(address);
        if (query == null) {
            throw new BadRequestException("error.geocoding.address-not-found");
        }
        List<OrsGeocodeResult> results = geocodingClient.search(query, 1, null, null);
        if (results.isEmpty()) {
            throw new BadRequestException("error.geocoding.address-not-found");
        }
        OrsGeocodeResult best = results.get(0);
        return new GeoPoint(best.lat(), best.lon());
    }

    private String toQuery(Address address) {
        if (address == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        appendPart(builder, address.getLine1());
        appendPart(builder, address.getPostalCode());
        appendPart(builder, address.getCity());
        String query = builder.toString().trim();
        return query.isEmpty() ? null : query;
    }

    private void appendPart(StringBuilder builder, String part) {
        if (part != null && !part.isBlank()) {
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(part.trim());
        }
    }
}
