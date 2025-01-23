package com.ecommerce.product_service.dto;

import com.ecommerce.product_service.config.onCreate;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductDTO {

    @NotBlank(message = "Please mention unique 'product'.")
    private String productName;

    @NotBlank(message = "Please provide description for the product.", groups = onCreate.class)
    private String description;

    @NotNull(message = "Please mention price of the unit.", groups = onCreate.class)
    private BigDecimal price;

    private Integer quantity;
}
