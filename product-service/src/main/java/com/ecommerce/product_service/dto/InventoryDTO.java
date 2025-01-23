package com.ecommerce.product_service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class InventoryDTO {

    @NotBlank(message = "Please mention unique 'product'.")
    private String product;

    @NotNull(message = "Please mention quantity.")
    private int quantity;
}
