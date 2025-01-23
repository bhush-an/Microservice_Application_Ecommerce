package com.ecommerce.order_service.dto;

import com.ecommerce.order_service.entity.PaymentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class OrderDTO {

    private String orderId;

    @NotNull(message = "Please provide products.")
    private Set<ProductDetailsDTO> products;

    private LocalDateTime orderDate;

    @NotBlank(message = "Missing customer in Headers section.")
    private String customer;

    private BigDecimal totalAmount;

    private PaymentStatus paymentStatus;
}
