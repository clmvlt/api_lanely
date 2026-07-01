package com.lanely.api.exception;

import org.springframework.http.HttpStatus;

public class AlreadyMemberException extends ApiException {

    public AlreadyMemberException(String messageKey, Object... args) {
        super(HttpStatus.CONFLICT, messageKey, args);
    }
}
