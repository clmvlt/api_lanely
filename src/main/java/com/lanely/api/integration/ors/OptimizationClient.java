package com.lanely.api.integration.ors;

import com.lanely.api.integration.ors.dto.OrsOptimizeRequest;
import com.lanely.api.integration.ors.dto.OrsOptimizeResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class OptimizationClient {

    private final RestClient orsRestClient;

    public OptimizationClient(RestClient orsRestClient) {
        this.orsRestClient = orsRestClient;
    }

    public OrsOptimizeResponse optimize(OrsOptimizeRequest request) {
        return OrsSupport.call(() -> orsRestClient.post()
                .uri("/optimization/optimize")
                .body(request)
                .retrieve()
                .body(OrsOptimizeResponse.class), "error.routing.unavailable", "error.optimization.failed");
    }
}
