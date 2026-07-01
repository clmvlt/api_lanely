package com.lanely.api.exception;

import org.springframework.http.HttpStatus;

public class VehiclePlateTakenException extends ApiException {

    public VehiclePlateTakenException(String messageKey, Object... args) {
        super(HttpStatus.CONFLICT, messageKey, args);
    }
}
