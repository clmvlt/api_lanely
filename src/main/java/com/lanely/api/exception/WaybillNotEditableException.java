package com.lanely.api.exception;

import org.springframework.http.HttpStatus;

public class WaybillNotEditableException extends ApiException {

    public WaybillNotEditableException(String messageKey, Object... args) {
        super(HttpStatus.CONFLICT, messageKey, args);
    }
}
