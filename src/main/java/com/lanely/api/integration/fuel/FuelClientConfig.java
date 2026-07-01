package com.lanely.api.integration.fuel;

import com.lanely.api.config.FuelProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class FuelClientConfig {

    @Bean
    public RestClient fuelRestClient(FuelProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout((int) properties.connectTimeoutOrDefault().toMillis());
        requestFactory.setReadTimeout((int) properties.readTimeoutOrDefault().toMillis());
        return RestClient.builder()
                .baseUrl(properties.baseUrl())
                .requestFactory((ClientHttpRequestFactory) requestFactory)
                .build();
    }
}
