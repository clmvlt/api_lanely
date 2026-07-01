package com.lanely.api.exception;

import org.springframework.http.HttpStatus;

public class OrsUnavailableException extends ApiException {

    public OrsUnavailableException(String messageKey, Object... args) {
        super(HttpStatus.SERVICE_UNAVAILABLE, messageKey, args);
    }
}
