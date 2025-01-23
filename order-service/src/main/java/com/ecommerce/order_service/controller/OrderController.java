package com.ecommerce.order_service.controller;

import com.ecommerce.order_service.config.OnCreate;
import com.ecommerce.order_service.dto.ProductDetailsDTO;
import com.ecommerce.order_service.dto.ResponseDTO;
import com.ecommerce.order_service.service.OrderService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/orders")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);
    @Autowired
    private OrderService orderService;

    @GetMapping("viewAll")
    public ResponseEntity<ResponseDTO> viewAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("view/{orderId}")
    public ResponseEntity<ResponseDTO> viewOrderById(@PathVariable String orderId) {
        return ResponseEntity.ok(orderService.getSpecificOrderDetails(orderId));
    }

    @GetMapping("viewProducts")
    public ResponseEntity<ResponseDTO> viewAllProducts() {
        return ResponseEntity.ok(orderService.getAllProducts());
    }

    @GetMapping("viewProducts/{product}")
    public ResponseEntity<ResponseDTO> viewProductByName(@PathVariable String product) {
        return ResponseEntity.ok(orderService.getProductByProductName(product));
    }

    @PostMapping("/create")
    public ResponseEntity<ResponseDTO> createOrder(@RequestBody @Validated(OnCreate.class)
                                                       List<ProductDetailsDTO> productDetailsDTOList,
                                                   @RequestHeader("customer") String customer) {
        log.info("customer from header: {}", customer);
        return new ResponseEntity<>(orderService.createOrder(productDetailsDTOList, customer), HttpStatus.CREATED);
    }

    @PostMapping("/createPaymentLink")
    public ResponseEntity<ResponseDTO> createOrderPaymentLink(@RequestParam String orderId) {
        return new ResponseEntity<>(orderService.createOrderPaymentLink(orderId), HttpStatus.CREATED);
    }

    @PutMapping("updateStatus")
    public ResponseEntity<Object> updatePaymentStatus(@RequestParam String orderId, @RequestParam String status) {
        orderService.updatePaymentStatus(orderId, status);
        return ResponseEntity.noContent().build();
    }
}
