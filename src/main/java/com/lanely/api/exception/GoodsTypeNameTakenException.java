package com.lanely.api.exception;

import org.springframework.http.HttpStatus;

public class GoodsTypeNameTakenException extends ApiException {

    public GoodsTypeNameTakenException(String messageKey, Object... args) {
        super(HttpStatus.CONFLICT, messageKey, args);
    }
}
