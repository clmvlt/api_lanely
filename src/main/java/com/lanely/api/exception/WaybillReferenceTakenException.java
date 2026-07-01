package com.lanely.api.exception;

import org.springframework.http.HttpStatus;

public class WaybillReferenceTakenException extends ApiException {

    public WaybillReferenceTakenException(String messageKey, Object... args) {
        super(HttpStatus.CONFLICT, messageKey, args);
    }
}
