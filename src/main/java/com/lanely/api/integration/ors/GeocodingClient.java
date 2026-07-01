package com.lanely.api.integration.ors;

import com.lanely.api.integration.ors.dto.OrsGeocodeResult;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

import java.util.List;
import java.util.function.Function;

@Component
public class GeocodingClient {

    private static final ParameterizedTypeReference<List<OrsGeocodeResult>> RESULT_LIST =
            new ParameterizedTypeReference<>() {
            };

    private final RestClient orsRestClient;

    public GeocodingClient(RestClient orsRestClient) {
        this.orsRestClient = orsRestClient;
    }

    public List<OrsGeocodeResult> search(String q, Integer limit, Double lat, Double lon) {
        Function<UriBuilder, java.net.URI> uri = builder -> {
            UriBuilder b = builder.path("/geocoding/search").queryParam("q", q);
            if (limit != null) {
                b = b.queryParam("limit", limit);
            }
            if (lat != null) {
                b = b.queryParam("lat", lat);
            }
            if (lon != null) {
                b = b.queryParam("lon", lon);
            }
            return b.build();
        };
        return OrsSupport.call(() -> orsRestClient.get()
                .uri(uri)
                .retrieve()
                .body(RESULT_LIST), "error.geocoding.unavailable", "error.geocoding.failed");
    }
}
