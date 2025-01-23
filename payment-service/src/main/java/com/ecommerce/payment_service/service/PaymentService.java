package com.ecommerce.payment_service.service;

import com.ecommerce.payment_service.config.OrderServiceProxy;
import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PaymentService {

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @Autowired
    private OrderServiceProxy orderProxy;

    public String createCheckoutSession(String orderId, BigDecimal totalAmount) throws Exception {
        Stripe.apiKey = stripeApiKey;
        SessionCreateParams params =
                SessionCreateParams.builder()
                        .setMode(SessionCreateParams.Mode.PAYMENT)
                        .setSuccessUrl("http://localhost:8300/api/payment/success?session_id={CHECKOUT_SESSION_ID}&orderId=" + orderId)
                        .setCancelUrl("http://localhost:8300/api/payment/cancel?session_id={CHECKOUT_SESSION_ID}&orderId=" + orderId)
                        .addLineItem(
                                SessionCreateParams.LineItem.builder()
                                        .setQuantity(1L)
                                        .setPriceData(
                                                SessionCreateParams.LineItem.PriceData.builder()
                                                        .setCurrency("INR")
                                                        .setUnitAmount(totalAmount.movePointRight(2).longValue()) // Convert to cents
                                                        .setProductData(
                                                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                        .setName("Order #" + orderId)
                                                                        .build()
                                                        )
                                                        .build()
                                        )
                                        .build()
                        )
                        .build();
        Session session = Session.create(params);
        return session.getUrl();
    }

    public String updateOrderPaymentStatus(String orderId, String status) {
        ResponseEntity<Object> response = orderProxy.updatePaymentStatus(orderId, status);
        if (response.getStatusCode().isSameCodeAs(HttpStatus.NO_CONTENT) && status.equalsIgnoreCase("success")) {
            return "SUCCESS";
        } else if (response.getStatusCode().isSameCodeAs(HttpStatus.NO_CONTENT) && status.equalsIgnoreCase("failed")) {
            return "FAILED";
        } else {
            return "NA";
        }
    }
}
