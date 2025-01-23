package com.ecommerce.product_service.service;

import com.ecommerce.product_service.config.InventoryServiceProxy;
import com.ecommerce.product_service.dto.*;
import com.ecommerce.product_service.entity.Product;
import com.ecommerce.product_service.exception.ProductNotFoundException;
import com.ecommerce.product_service.exception.QuantityNotMentionedException;
import com.ecommerce.product_service.exception.ServiceNotAvailableException;
import com.ecommerce.product_service.repository.IProductRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class ProductService {

    @Autowired
    private ModelMapper mapper;

    @Autowired
    private IProductRepository productRepo;

    @Autowired
    private InventoryServiceProxy inventoryProxy;

    @CircuitBreaker(name = "inventory-service", fallbackMethod = "inventoryServiceFallback")
    public ResponseDTO getAllProducts() {
        ResponseDTO responseDTO = inventoryProxy.getAllInventoryProducts().getBody();
        if (responseDTO == null || responseDTO.getListOfInventories() == null) {
            return ResponseDTO.builder()
                    .message("No products found in the inventory.")
                    .listOfProducts(Collections.emptyList())
                    .build();
        }
        List<InventoryDTO> inventories = responseDTO.getListOfInventories();
        List<String> productNames = inventories.stream()
                .map(InventoryDTO::getProduct)
                .toList();
        List<Product> products = productRepo.findByProductNameIn(productNames);
        List<ProductDTO> productDTOS = products.stream()
                .map(product -> mapper.map(product, ProductDTO.class))
                .toList();
        return ResponseDTO.builder()
                .message("Product List fetched successfully!")
                .listOfProducts(productDTOS)
                .build();
    }

    public ResponseDTO getProductByProductName(String productName) {
        Product product = productRepo.findByProductName(productName)
                .orElseThrow(() -> new ProductNotFoundException("Invalid Product!"));
        ProductResponseDTO productDTO = mapper.map(product, ProductResponseDTO.class);
        return ResponseDTO.builder()
                .message("Product found: " + productName)
                .productDetails(productDTO)
                .build();
    }

    @CircuitBreaker(name = "inventory-service", fallbackMethod = "inventoryServiceFallback")
    public ResponseDTO addProduct(ProductDTO productDTO) {
        if (productDTO.getQuantity() == null) {
            throw new QuantityNotMentionedException("Please mention parameter: quantity.");
        }
        Product product = mapper.map(productDTO, Product.class);
        ProductResponseDTO productResponseDTO = null;
        boolean newProduct = productRepo.findByProductName(product.getProductName()).isEmpty();
        if (newProduct) {
            Product persistentProduct = productRepo.save(product);
            productResponseDTO = mapper.map(persistentProduct, ProductResponseDTO.class);
        }
        InventoryDTO inventoryDTO = mapper.map(productDTO, InventoryDTO.class);
        ResponseEntity<ResponseDTO> response = inventoryProxy.upsertProductInInventory(inventoryDTO);
        if (response.getStatusCode().is2xxSuccessful()) {
            String message = newProduct ? " added successfully!" : " already exists! Updating quantity for this product...";
            return ResponseDTO.builder()
                    .message("Product: " + productDTO.getProductName() + message)
                    .productDetails(productResponseDTO)
                    .inventoryDetails(Objects.requireNonNull(response.getBody()).getInventoryDetails())
                    .upsertedInventoryDetails(Objects.requireNonNull(response.getBody()).getUpsertedInventoryDetails())
                    .build();
        } else {
            throw new RuntimeException("Inventory-Service");
        }
    }

    @CircuitBreaker(name = "inventory-service", fallbackMethod = "inventoryServiceFallback")
    public ResponseDTO editProduct(ProductDTO productDTO) {
        Product product = productRepo.findByProductName(productDTO.getProductName())
                .orElseThrow(() -> new ProductNotFoundException("Please provide a valid Product Name!"));
        if (productDTO.getDescription() != null && !productDTO.getDescription().isEmpty()) {
            product.setDescription(productDTO.getDescription());
        }
        if (productDTO.getPrice() != null) {
            product.setPrice(productDTO.getPrice());
        }
        Product persistentProduct = productRepo.save(product);
        ProductResponseDTO productResponseDTO = mapper.map(persistentProduct, ProductResponseDTO.class);

        InventoryUpsertDTO inventoryResponseDTO = null;
        if (productDTO.getQuantity() != null) {
            InventoryDTO inventoryDTO = mapper.map(productDTO, InventoryDTO.class);
            inventoryResponseDTO = Objects.requireNonNull(inventoryProxy.upsertProductInInventory(inventoryDTO)
                    .getBody()).getUpsertedInventoryDetails();
        }
        return ResponseDTO.builder()
                .message("Product: " + productDTO.getProductName() + " updated successfully!")
                .productDetails(productResponseDTO)
                .upsertedInventoryDetails(inventoryResponseDTO)
                .build();
    }

    @CircuitBreaker(name = "inventory-service", fallbackMethod = "inventoryServiceDeleteFallback")
    public void deleteProduct(String productName) {
        Product product = productRepo.findByProductName(productName)
                .orElseThrow(() -> new ProductNotFoundException("Please provide a valid Product Name!"));
        ResponseEntity<Object> response = inventoryProxy.removeProductFromInventory(productName);
        if (response.getStatusCode().isSameCodeAs(HttpStatus.NO_CONTENT)) {
            productRepo.delete(product);
        } else {
            throw new RuntimeException("Inventory-Service");
        }
    }

    public ResponseDTO inventoryServiceFallback(Throwable t) {
        throw new ServiceNotAvailableException("inventory-service");
    }

    public void inventoryServiceDeleteFallback(Throwable t) {
        throw new ServiceNotAvailableException("inventory-service");
    }

}
