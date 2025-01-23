package com.ecommerce.product_service.config;

import com.ecommerce.product_service.dto.InventoryDTO;
import com.ecommerce.product_service.dto.ResponseDTO;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

//@FeignClient(name = "inventory-service", url = "http://localhost:8000")
@FeignClient(name = "inventory-service")
public interface InventoryServiceProxy {

    @GetMapping("api/inventory/all")
    ResponseEntity<ResponseDTO> getAllInventoryProducts();

    @GetMapping("api/inventory/available")
    ResponseEntity<Boolean> checkInventoryAvailability(@RequestParam String product);

    @GetMapping("api/inventory/available")
    ResponseEntity<Boolean> checkInventoryAvailability(@RequestParam String product,
                                                       @RequestParam(required = false) Integer quantity);

    @PostMapping("api/inventory/upsert")
    ResponseEntity<ResponseDTO> upsertProductInInventory(@RequestBody @Valid InventoryDTO inventoryDTO);

    @DeleteMapping("api/inventory/remove")
    ResponseEntity<Object> removeProductFromInventory(@RequestParam String productName);
}
