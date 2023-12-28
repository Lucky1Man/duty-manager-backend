package com.duty.manager.service;


import lombok.Generated;
import org.springframework.http.HttpStatus;

@Generated
public class ServiceException extends RuntimeException {

    private final HttpStatus statusCode;

    public ServiceException(String message) {
        this(message, HttpStatus.BAD_REQUEST);
    }

    public ServiceException(String message, HttpStatus statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public HttpStatus getStatusCode() {
        return statusCode;
    }
}
