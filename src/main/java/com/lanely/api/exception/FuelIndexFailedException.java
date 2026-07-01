package com.lanely.api.exception;

import org.springframework.http.HttpStatus;

public class FuelIndexFailedException extends ApiException {

    public FuelIndexFailedException(String messageKey, Object... args) {
        super(HttpStatus.BAD_GATEWAY, messageKey, args);
    }
}
