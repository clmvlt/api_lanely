package com.lanely.api.exception;

import org.springframework.http.HttpStatus;

public class InvitationNotPendingException extends ApiException {

    public InvitationNotPendingException(String messageKey, Object... args) {
        super(HttpStatus.CONFLICT, messageKey, args);
    }
}
