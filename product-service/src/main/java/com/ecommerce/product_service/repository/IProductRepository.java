package com.ecommerce.product_service.repository;

import com.ecommerce.product_service.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

@Repository
public interface IProductRepository extends JpaRepository<Product, String> {

    Optional<Product> findByProductName(String name);

    @Query("SELECT p FROM Product p WHERE p.productName = :productName")
    Product findByProduct(String productName);

    List<Product> findByProductNameIn(List<String> productNames);
}
