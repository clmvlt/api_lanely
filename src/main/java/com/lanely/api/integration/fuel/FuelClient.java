package com.lanely.api.integration.fuel;

import com.fasterxml.jackson.databind.JsonNode;
import com.lanely.api.config.FuelProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class FuelClient {

    private final RestClient fuelRestClient;
    private final FuelProperties properties;
    private final FuelPriceParser parser;

    public FuelClient(RestClient fuelRestClient, FuelProperties properties, FuelPriceParser parser) {
        this.fuelRestClient = fuelRestClient;
        this.properties = properties;
        this.parser = parser;
    }

    /**
     * Fetches the configured government dataset and returns the normalized fuel price samples it
     * contains. The raw JSON layout is decoded by {@link FuelPriceParser} according to the
     * configured record/field mapping, keeping this client source-agnostic.
     */
    public List<FuelPriceSample> fetchLatest() {
        JsonNode body = FuelSupport.call(() -> fuelRestClient.get()
                .uri(properties.datasetPath())
                .retrieve()
                .body(JsonNode.class), "error.fuel.unavailable", "error.fuel.failed");
        return parser.parse(body);
    }
}
