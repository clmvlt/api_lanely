package com.lanely.api.exception;

import org.springframework.http.HttpStatus;

public class FuelIndexUnavailableException extends ApiException {

    public FuelIndexUnavailableException(String messageKey, Object... args) {
        super(HttpStatus.SERVICE_UNAVAILABLE, messageKey, args);
    }
}
