package com.iyzipay.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT)
public class SeatNotAvailableException extends RuntimeException{
    public SeatNotAvailableException(String message) {
        super(message);
    }
}
