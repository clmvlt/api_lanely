package com.lanely.api.exception;

import org.springframework.http.HttpStatus;

public class InvitationExpiredException extends ApiException {

    public InvitationExpiredException(String messageKey, Object... args) {
        super(HttpStatus.CONFLICT, messageKey, args);
    }
}
