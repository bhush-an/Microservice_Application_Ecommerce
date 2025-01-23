package com.ecommerce.inventory_service.repository;

import com.ecommerce.inventory_service.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IInventoryRepository extends JpaRepository<Inventory, String> {

    Inventory findByProduct(String productName);
}
