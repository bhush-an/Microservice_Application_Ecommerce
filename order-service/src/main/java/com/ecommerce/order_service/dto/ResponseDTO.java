package com.ecommerce.order_service.dto;

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

    private Integer errorCode;

    private String service;

    private String errorMessage;

    private InventoryDTO inventoryDetails;

    private OrderDTO orderDetails;

    private String paymentUrl;

    private List<OrderDTO> listOfOrderDetails;

    private ProductResponseDTO productDetails;

    private List<ProductResponseDTO> listOfProducts;
}
