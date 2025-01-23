package com.ecommerce.order_service.config;

import com.ecommerce.order_service.dto.InventoryDTO;
import com.ecommerce.order_service.dto.ResponseDTO;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

//@FeignClient(name = "inventory-service", url = "http://localhost:8000")
@FeignClient(name = "inventory-service")
public interface InventoryServiceProxy {

    @GetMapping("api/inventory/{product}")
    ResponseEntity<ResponseDTO> getProductFromInventory(@PathVariable String product);

    @GetMapping("api/inventory/available")
    ResponseEntity<Boolean> checkInventoryAvailability(@RequestParam String product);

    @GetMapping("api/inventory/available")
    ResponseEntity<Boolean> checkInventoryAvailability(@RequestParam String product,
                                                       @RequestParam(required = false) Integer quantity);

    @PutMapping("api/inventory/update")
    void updateProductInInventory(@RequestBody @Valid InventoryDTO inventoryDTO);

    @PutMapping("api/inventory/increase")
    void increaseProductInInventory(@RequestBody @Valid InventoryDTO inventoryDTO);
}
