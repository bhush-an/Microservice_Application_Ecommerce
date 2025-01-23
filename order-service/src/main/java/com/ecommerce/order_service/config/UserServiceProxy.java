package com.ecommerce.order_service.config;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

//@FeignClient(name = "user-service", url = "http://localhost:8400")
@FeignClient(name = "user-service")
public interface UserServiceProxy {

    @GetMapping("api/auth/checkCustomer/{email}")
    String checkCustomer(@PathVariable String email);
}
