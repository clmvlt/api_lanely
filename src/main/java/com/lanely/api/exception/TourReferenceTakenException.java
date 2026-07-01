package com.lanely.api.exception;

import org.springframework.http.HttpStatus;

public class TourReferenceTakenException extends ApiException {

    public TourReferenceTakenException(String messageKey, Object... args) {
        super(HttpStatus.CONFLICT, messageKey, args);
    }
}
