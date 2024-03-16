package com.iyzipay.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.PRECONDITION_FAILED)
public class PaymentNotAcceptedException extends RuntimeException{
    public PaymentNotAcceptedException(String message) {
        super(message);
    }
}
