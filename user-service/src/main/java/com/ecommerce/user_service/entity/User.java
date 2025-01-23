package com.ecommerce.user_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String userId;

    @Column(length = 30, nullable = false, unique = true)
    private String emailId;

    @Column(length = 150, nullable = false)
    private String password;

    @Transient
    @Column(length = 150, nullable = false)
    private String confirmPassword;

    @Column(length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole userRole;
}
