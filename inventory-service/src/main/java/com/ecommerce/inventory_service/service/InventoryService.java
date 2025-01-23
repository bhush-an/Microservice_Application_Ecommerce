package com.ecommerce.inventory_service.service;

import com.ecommerce.inventory_service.dto.InventoryDTO;
import com.ecommerce.inventory_service.dto.InventoryUpsertDTO;
import com.ecommerce.inventory_service.dto.ResponseDTO;
import com.ecommerce.inventory_service.entity.Inventory;
import com.ecommerce.inventory_service.exception.ProductNotFoundException;
import com.ecommerce.inventory_service.repository.IInventoryRepository;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class InventoryService {

    @Autowired
    private ModelMapper mapper;

    @Autowired
    private IInventoryRepository inventoryRepo;

    public ResponseDTO viewAllProducts() {
        List<Inventory> inventories = inventoryRepo.findAll();
        List<InventoryDTO> inventoryDTOList = inventories.stream()
                .map(inventory -> mapper.map(inventory, InventoryDTO.class))
                .toList();
        return ResponseDTO.builder()
                .listOfInventories(inventoryDTOList)
                .build();
    }

    public ResponseDTO viewProduct(String product) {
        Inventory inventory = inventoryRepo.findByProduct(product);
        if (inventory == null) {
            throw new ProductNotFoundException("This product does not exist in inventory: " + product);
        }
        InventoryDTO inventoryDTO = mapper.map(inventory, InventoryDTO.class);
        return ResponseDTO.builder()
                .inventoryDetails(inventoryDTO)
                .build();
    }

    public boolean checkProductAvailability(String productName, Integer quantity) {
        Inventory inventory = inventoryRepo.findByProduct(productName);
        if (inventory == null) {
            throw new ProductNotFoundException("This product does not exist in inventory: " + productName);
        }
        return inventory.getQuantity() >= Objects.requireNonNullElse(quantity, 1);
    }

    public ResponseDTO upsertInventory(InventoryDTO inventoryDTO) {
        Inventory inventory = inventoryRepo.findByProduct(inventoryDTO.getProduct());
        boolean isNew = inventory == null;
        if (isNew) {
            Inventory transientInventory = mapper.map(inventoryDTO, Inventory.class);
            Inventory persistentInventory = inventoryRepo.save(transientInventory);
            InventoryDTO inventoryResponseDTO = mapper.map(persistentInventory, InventoryDTO.class);
            return ResponseDTO.builder()
                    .message("Product added in Inventory successfully!")
                    .inventoryDetails(inventoryResponseDTO)
                    .build();
        } else {
            int oldQty = inventory.getQuantity();
            int newQty = inventory.getQuantity() + inventoryDTO.getQuantity();
            inventory.setQuantity(newQty);
            Inventory persistentInventory = inventoryRepo.save(inventory);
            InventoryUpsertDTO inventoryResponseDTO = mapper.map(persistentInventory, InventoryUpsertDTO.class);
            inventoryResponseDTO.setOldQuantity(oldQty);
            inventoryResponseDTO.setUpdatedQuantity(newQty);
            return ResponseDTO.builder()
                    .message("Product updated in Inventory successfully!")
                    .upsertedInventoryDetails(inventoryResponseDTO)
                    .build();
        }
    }

    public void updateInventory(InventoryDTO inventoryDTO) {
        Inventory inventory = inventoryRepo.findByProduct(inventoryDTO.getProduct());
        inventory.setQuantity(inventory.getQuantity() - inventoryDTO.getQuantity());
        inventoryRepo.save(inventory);
    }

    public void increaseProductQuantityInInventory(InventoryDTO inventoryDTO) {
        Inventory inventory = inventoryRepo.findByProduct(inventoryDTO.getProduct());
        inventory.setQuantity(inventory.getQuantity() + inventoryDTO.getQuantity());
        inventoryRepo.save(inventory);
    }

    public void deleteInventory(String productName) {
        Inventory inventory = inventoryRepo.findByProduct(productName);
        if (inventory == null) {
            throw new ProductNotFoundException("This product does not exist: " + productName);
        }
        inventoryRepo.delete(inventory);
    }
}
