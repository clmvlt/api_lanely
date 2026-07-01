package com.lanely.api.exception;

import org.springframework.http.HttpStatus;

public class NotCompanyOwnerException extends ApiException {

    public NotCompanyOwnerException(String messageKey, Object... args) {
        super(HttpStatus.FORBIDDEN, messageKey, args);
    }
}
