package com.daeliin.components.webservices.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ResourceUpdateRequestException extends RuntimeException {

    public ResourceUpdateRequestException(String message) {
        super(message);
    }
}
