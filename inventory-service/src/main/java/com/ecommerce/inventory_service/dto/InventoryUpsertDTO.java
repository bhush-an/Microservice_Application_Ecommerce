package com.ecommerce.inventory_service.dto;

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
public class InventoryUpsertDTO {

    private Long id;

    @NotBlank(message = "Please mention 'product'.")
    private String product;

    @NotNull(message = "Please mention quantity.")
    private int quantity;

    private int oldQuantity;

    private int updatedQuantity;
}
