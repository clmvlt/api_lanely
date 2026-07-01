package com.lanely.api.integration.fuel;

import com.lanely.api.exception.FuelIndexFailedException;
import com.lanely.api.exception.FuelIndexUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

import java.util.function.Supplier;

/**
 * Translates failures from the government fuel price API into localized
 * {@link com.lanely.api.exception.ApiException}s. A 503 upstream or a connectivity error becomes a
 * 503 with {@code unavailableKey}; any other error becomes a 502 with {@code failedKey}.
 */
final class FuelSupport {

    private static final Logger log = LoggerFactory.getLogger(FuelSupport.class);

    private FuelSupport() {
    }

    static <T> T call(Supplier<T> supplier, String unavailableKey, String failedKey) {
        try {
            return supplier.get();
        } catch (RestClientResponseException ex) {
            int statusCode = ex.getStatusCode().value();
            if (statusCode == 503) {
                log.warn("Fuel price subsystem unavailable (HTTP 503): {}", ex.getResponseBodyAsString());
                throw new FuelIndexUnavailableException(unavailableKey);
            }
            log.warn("Fuel price call failed (HTTP {}): {}", statusCode, ex.getResponseBodyAsString());
            throw new FuelIndexFailedException(failedKey);
        } catch (ResourceAccessException ex) {
            log.warn("Fuel price call could not reach the server: {}", ex.getMessage());
            throw new FuelIndexUnavailableException(unavailableKey);
        } catch (RuntimeException ex) {
            log.error("Unexpected error calling the fuel price API", ex);
            throw new FuelIndexFailedException(failedKey);
        }
    }
}
