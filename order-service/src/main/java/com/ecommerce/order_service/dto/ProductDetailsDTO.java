package com.ecommerce.order_service.dto;

import com.ecommerce.order_service.config.OnCreate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ProductDetailsDTO {

    @NotBlank(message = "Please provide product.")
    private String product;

    @NotNull(message = "Please provide quantity for the product.", groups = OnCreate.class)
    private Integer quantity;

    private BigDecimal unitPrice;
}
