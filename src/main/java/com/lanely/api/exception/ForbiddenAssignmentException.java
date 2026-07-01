package com.lanely.api.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenAssignmentException extends ApiException {

    public ForbiddenAssignmentException(String messageKey, Object... args) {
        super(HttpStatus.FORBIDDEN, messageKey, args);
    }
}
