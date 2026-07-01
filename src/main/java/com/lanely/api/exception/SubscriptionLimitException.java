package com.lanely.api.exception;

import org.springframework.http.HttpStatus;

public class SubscriptionLimitException extends ApiException {

    public SubscriptionLimitException(String messageKey, Object... args) {
        super(HttpStatus.FORBIDDEN, messageKey, args);
    }
}
