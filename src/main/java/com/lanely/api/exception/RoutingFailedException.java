package com.lanely.api.exception;

import org.springframework.http.HttpStatus;

public class RoutingFailedException extends ApiException {

    public RoutingFailedException(String messageKey, Object... args) {
        super(HttpStatus.BAD_GATEWAY, messageKey, args);
    }
}
