package com.lanely.api.exception;

import org.springframework.http.HttpStatus;

public class MissingPermissionException extends ApiException {

    public MissingPermissionException(String messageKey, Object... args) {
        super(HttpStatus.FORBIDDEN, messageKey, args);
    }
}
