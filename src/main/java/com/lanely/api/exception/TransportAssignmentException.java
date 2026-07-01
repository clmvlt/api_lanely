package com.lanely.api.exception;

import org.springframework.http.HttpStatus;

public class TransportAssignmentException extends ApiException {

    public TransportAssignmentException(String messageKey, Object... args) {
        super(HttpStatus.CONFLICT, messageKey, args);
    }
}
