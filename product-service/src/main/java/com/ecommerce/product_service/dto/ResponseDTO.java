package com.ecommerce.product_service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseDTO {

    private String message;

    private String service;

    private Integer errorCode;

    private String errorMessage;

    private ProductResponseDTO productDetails;

    private InventoryDTO inventoryDetails;

    private InventoryUpsertDTO upsertedInventoryDetails;

    private List<InventoryDTO> listOfInventories;

    private List<ProductDTO> listOfProducts;
}
