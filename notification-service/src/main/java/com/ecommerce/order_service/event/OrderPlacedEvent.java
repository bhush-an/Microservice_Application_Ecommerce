package com.ecommerce.order_service.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class OrderPlacedEvent {

    private String orderId;

    private String email;

    private String paymentUrl;
}
