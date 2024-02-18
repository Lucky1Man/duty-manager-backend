package com.duty.manager.controller;

import com.duty.manager.service.ServiceException;
import io.jsonwebtoken.ClaimJwtException;
import lombok.Generated;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;


@RestControllerAdvice
@Generated
public class ControllerExceptionHandler {

    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionResponse handleConstraintViolationException(jakarta.validation.ConstraintViolationException e) {
        return ExceptionResponse.builder()
                .withMessage(e.getMessage())
                .withHttpStatus(HttpStatus.BAD_REQUEST)
                .withDate(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(org.hibernate.exception.ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionResponse handleHibernateConstraintViolationException(
            org.hibernate.exception.ConstraintViolationException e) {
        return ExceptionResponse.builder()
                .withMessage(e.getMessage())
                .withHttpStatus(HttpStatus.BAD_REQUEST)
                .withDate(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(ServiceException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionResponse handleHibernateConstraintViolationException(
            ServiceException e) {
        return ExceptionResponse.builder()
                .withMessage(e.getMessage())
                .withHttpStatus(e.getStatusCode())
                .withDate(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(ConversionFailedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionResponse handleConversionFailedException(
            ConversionFailedException e) {
        return ExceptionResponse.builder()
                .withMessage(e.getMessage())
                .withHttpStatus(HttpStatus.BAD_REQUEST)
                .withDate(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(ClaimJwtException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionResponse handleConversionFailedException(
            ClaimJwtException e) {
        return ExceptionResponse.builder()
                .withMessage(e.getMessage())
                .withHttpStatus(HttpStatus.BAD_REQUEST)
                .withDate(LocalDateTime.now())
                .build();
    }

}
