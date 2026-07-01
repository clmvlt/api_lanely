package com.lanely.api.service;

import com.lanely.api.entity.embeddable.Address;
import com.lanely.api.exception.ApiException;
import com.lanely.api.integration.ors.GeocodingClient;
import com.lanely.api.integration.ors.dto.OrsGeocodeResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

@Service
public class AddressGeocodingService {

    private static final Logger log = LoggerFactory.getLogger(AddressGeocodingService.class);

    private final GeocodingClient geocodingClient;

    public AddressGeocodingService(GeocodingClient geocodingClient) {
        this.geocodingClient = geocodingClient;
    }

    public Optional<OrsGeocodeResult> resolve(Address address) {
        String query = buildQuery(address);
        if (query == null) {
            return Optional.empty();
        }
        try {
            List<OrsGeocodeResult> results = geocodingClient.search(query, 1, null, null);
            if (results == null || results.isEmpty()) {
                log.debug("Geocoding returned no result for query '{}'", query);
                return Optional.empty();
            }
            OrsGeocodeResult best = results.get(0);
            if (best.lat() == null || best.lon() == null) {
                return Optional.empty();
            }
            return Optional.of(best);
        } catch (ApiException ex) {
            log.warn("Geocoding unavailable for query '{}', skipping auto coordinates: {}", query, ex.getMessageKey());
            return Optional.empty();
        }
    }

    private String buildQuery(Address address) {
        if (address == null) {
            return null;
        }
        if (address.getLine1() == null && address.getCity() == null && address.getPostalCode() == null) {
            return null;
        }
        StringJoiner joiner = new StringJoiner(", ");
        append(joiner, address.getLine1());
        append(joiner, address.getPostalCode());
        append(joiner, address.getCity());
        append(joiner, address.getState());
        append(joiner, address.getCountry());
        String query = joiner.toString();
        return query.isEmpty() ? null : query;
    }

    private void append(StringJoiner joiner, String value) {
        if (value != null && !value.isBlank()) {
            joiner.add(value.trim());
        }
    }
}
