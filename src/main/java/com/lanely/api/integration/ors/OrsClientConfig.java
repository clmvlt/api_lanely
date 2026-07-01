package com.lanely.api.integration.ors;

import com.lanely.api.config.OrsProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class OrsClientConfig {

    @Bean
    public RestClient orsRestClient(OrsProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout((int) properties.connectTimeoutOrDefault().toMillis());
        requestFactory.setReadTimeout((int) properties.readTimeoutOrDefault().toMillis());
        return RestClient.builder()
                .baseUrl(properties.baseUrl())
                .requestFactory((ClientHttpRequestFactory) requestFactory)
                .build();
    }
}
