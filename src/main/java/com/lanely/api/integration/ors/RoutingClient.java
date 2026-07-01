package com.lanely.api.integration.ors;

import com.lanely.api.integration.ors.dto.OrsRouteRequest;
import com.lanely.api.integration.ors.dto.OrsRouteResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class RoutingClient {

    private final RestClient orsRestClient;

    public RoutingClient(RestClient orsRestClient) {
        this.orsRestClient = orsRestClient;
    }

    public OrsRouteResponse route(OrsRouteRequest request) {
        return OrsSupport.call(() -> orsRestClient.post()
                .uri("/routing/route")
                .body(request)
                .retrieve()
                .body(OrsRouteResponse.class), "error.routing.unavailable", "error.routing.failed");
    }
}
