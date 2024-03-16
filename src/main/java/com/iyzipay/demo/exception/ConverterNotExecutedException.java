package com.iyzipay.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT)
public class ConverterNotExecutedException extends RuntimeException{

    public ConverterNotExecutedException(String message) {
        super(message);
    }
}
