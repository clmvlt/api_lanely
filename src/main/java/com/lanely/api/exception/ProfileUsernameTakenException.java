package com.lanely.api.exception;

import org.springframework.http.HttpStatus;

public class ProfileUsernameTakenException extends ApiException {

    public ProfileUsernameTakenException(String messageKey, Object... args) {
        super(HttpStatus.CONFLICT, messageKey, args);
    }
}
