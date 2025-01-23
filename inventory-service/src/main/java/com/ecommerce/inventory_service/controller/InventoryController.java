package com.ecommerce.inventory_service.controller;

import com.ecommerce.inventory_service.dto.InventoryDTO;
import com.ecommerce.inventory_service.dto.ResponseDTO;
import com.ecommerce.inventory_service.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @GetMapping("all")
    public ResponseEntity<ResponseDTO> getAllInventoryProducts() {
        return ResponseEntity.ok(inventoryService.viewAllProducts());
    }

    @GetMapping("{product}")
    public ResponseEntity<ResponseDTO> getProductFromInventory(@PathVariable String product) {
        return ResponseEntity.ok(inventoryService.viewProduct(product));
    }

    @GetMapping("available")
    public ResponseEntity<Boolean> checkInventoryAvailability(@RequestParam String product,
                                                              @RequestParam(required = false) Integer quantity) {
        return ResponseEntity.ok(inventoryService.checkProductAvailability(product, quantity));
    }

    @PostMapping("upsert")
    public ResponseEntity<ResponseDTO> upsertProductInInventory(@RequestBody @Valid InventoryDTO inventoryDTO) {
        return new ResponseEntity<>(inventoryService.upsertInventory(inventoryDTO), HttpStatus.CREATED);
    }

    @PutMapping("update")
    public ResponseEntity<Object> updateProductInInventory(@RequestBody @Valid InventoryDTO inventoryDTO) {
        inventoryService.updateInventory(inventoryDTO);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("increase")
    public ResponseEntity<Object> increaseProductInInventory(@RequestBody @Valid InventoryDTO inventoryDTO) {
        inventoryService.increaseProductQuantityInInventory(inventoryDTO);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("remove")
    public ResponseEntity<Object> removeProductFromInventory(@RequestParam String productName) {
        inventoryService.deleteInventory(productName);
        return ResponseEntity.noContent().build();
    }
}
