package com.lanely.api.exception;

import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends ApiException {

    public InvalidCredentialsException(String messageKey, Object... args) {
        super(HttpStatus.UNAUTHORIZED, messageKey, args);
    }
}
