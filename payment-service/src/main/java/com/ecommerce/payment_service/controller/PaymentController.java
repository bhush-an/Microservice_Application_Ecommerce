package com.ecommerce.payment_service.controller;

import com.ecommerce.payment_service.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/create/{orderId}/{amount}")
    public ResponseEntity<String> createPaymentLink(@PathVariable String orderId, @PathVariable BigDecimal amount) {
        try {
            String paymentUrl = paymentService.createCheckoutSession(orderId, amount);
            return new ResponseEntity<>(paymentUrl, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to create payment link!");
        }
    }

    @GetMapping("success")
    public ModelAndView paymentSuccess(@RequestParam String session_id, @RequestParam String orderId) {
        System.out.println("================== SUCCESSFUL ======================");
        String orderMessage = paymentService.updateOrderPaymentStatus(orderId, "success");

        ModelAndView modelAndView = new ModelAndView("paymentSuccess");
        modelAndView.addObject("sessionId", session_id);
        modelAndView.addObject("orderId", orderId);
        modelAndView.addObject("orderMessage", orderMessage);
        return modelAndView;
    }

    @GetMapping("cancel")
    public ModelAndView paymentCancel(@RequestParam String session_id, @RequestParam String orderId) {
        System.out.println("================== FAILED ======================");
        String orderMessage = paymentService.updateOrderPaymentStatus(orderId, "failed");

        ModelAndView modelAndView = new ModelAndView("paymentCancel");
        modelAndView.addObject("orderMessage", orderMessage);
        return modelAndView;
    }
}
