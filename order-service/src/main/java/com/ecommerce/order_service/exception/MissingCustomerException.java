package com.ecommerce.order_service.exception;

public class MissingCustomerException extends RuntimeException {
    public MissingCustomerException(String message) {
        super(message);
    }
}
