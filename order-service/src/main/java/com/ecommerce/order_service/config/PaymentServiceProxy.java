package com.ecommerce.order_service.config;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.math.BigDecimal;

//@FeignClient(name = "payment-service", url = "http://localhost:8300")
@FeignClient(name = "payment-service")
public interface PaymentServiceProxy {

    @PostMapping("api/payment/create/{orderId}/{amount}")
    ResponseEntity<String> createPaymentLink(@PathVariable String orderId, @PathVariable BigDecimal amount);
}
