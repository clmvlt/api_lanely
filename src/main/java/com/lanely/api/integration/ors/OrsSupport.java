package com.lanely.api.integration.ors;

import com.lanely.api.exception.OrsUnavailableException;
import com.lanely.api.exception.RoutingFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

import java.util.function.Supplier;

/**
 * Translates failures from the ors.stack.bzh API into localized {@link com.lanely.api.exception.ApiException}s.
 * A 503 upstream (data being (re)built) or a connectivity error becomes a 503 with {@code unavailableKey};
 * any other error becomes a 502 with {@code failedKey}.
 */
final class OrsSupport {

    private static final Logger log = LoggerFactory.getLogger(OrsSupport.class);

    private OrsSupport() {
    }

    static <T> T call(Supplier<T> supplier, String unavailableKey, String failedKey) {
        try {
            return supplier.get();
        } catch (RestClientResponseException ex) {
            int statusCode = ex.getStatusCode().value();
            if (statusCode == 503) {
                log.warn("ORS subsystem unavailable (HTTP 503): {}", ex.getResponseBodyAsString());
                throw new OrsUnavailableException(unavailableKey);
            }
            log.warn("ORS call failed (HTTP {}): {}", statusCode, ex.getResponseBodyAsString());
            throw new RoutingFailedException(failedKey);
        } catch (ResourceAccessException ex) {
            log.warn("ORS call could not reach the server: {}", ex.getMessage());
            throw new OrsUnavailableException(unavailableKey);
        } catch (RuntimeException ex) {
            log.error("Unexpected error calling ORS", ex);
            throw new RoutingFailedException(failedKey);
        }
    }
}
