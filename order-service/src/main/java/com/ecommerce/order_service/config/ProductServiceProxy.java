package com.ecommerce.order_service.config;

import com.ecommerce.order_service.dto.ResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

//@FeignClient(name = "product-service", url = "http://localhost:8100")
@FeignClient(name = "product-service")
public interface ProductServiceProxy {

    @GetMapping("api/products/view")
    ResponseEntity<ResponseDTO> fetchAllProducts();

    @GetMapping("api/products/view/{product}")
    ResponseEntity<ResponseDTO> getProductDetailsById(@PathVariable String product);

}
