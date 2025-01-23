package com.ecommerce.inventory_service.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Inventory {

    @Id
    @Column(length = 50, nullable = false, unique = true)
    private String product;

    @Column(nullable = false)
    private int quantity;

}
