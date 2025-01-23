package com.ecommerce.payment_service.controller;

import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/webhook")
public class WebhookController {

    @PostMapping("/stripe")
    public ResponseEntity<String> handleStripeWebhook(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader) {
        String webhookSecret = "your_webhook_secret"; // Replace with your actual webhook secret

        try {
            Event event = Webhook.constructEvent(payload, sigHeader, webhookSecret);

            if ("checkout.session.completed".equals(event.getType())) {
                // Update the order status to PAID in the Order Service
                Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
                if (session != null) {
                    // Now you can safely access properties of the session
                    String orderId = session.getMetadata().get("orderId"); // Assuming orderId is stored in metadata
                    // Call Order Service to update payment status



                } else {
                    // Handle the case where session is null (optional)
                    System.out.println("Session is null");
                }

            }
            return ResponseEntity.ok("Webhook received and processed");
        } catch (Exception e) {
            return ResponseEntity.status(400).body("Webhook error");
        }
    }

}
