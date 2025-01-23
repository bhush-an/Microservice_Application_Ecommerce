package com.ecommerce.order_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "orders")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String orderId;

    @ElementCollection
    @CollectionTable(name = "products_order",
            joinColumns = @JoinColumn(name = "order_id"))
    private Set<ProductDetails> products = new HashSet<>();

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime orderDate;

    @Column(length = 100, nullable = false)
    private String customer;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;
}
