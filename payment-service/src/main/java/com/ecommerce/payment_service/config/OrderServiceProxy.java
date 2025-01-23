package com.ecommerce.payment_service.config;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

//@FeignClient(name = "order-service", url = "http://localhost:8200")
@FeignClient(name = "order-service")
public interface OrderServiceProxy {

    @PutMapping("/api/orders/updateStatus")
    ResponseEntity<Object> updatePaymentStatus(@RequestParam String orderId, @RequestParam String status);

}
