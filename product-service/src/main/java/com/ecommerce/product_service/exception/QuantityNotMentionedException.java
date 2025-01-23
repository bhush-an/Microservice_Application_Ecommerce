package com.ecommerce.product_service.exception;

public class QuantityNotMentionedException extends RuntimeException {
    public QuantityNotMentionedException(String message) {
        super(message);
    }
}
