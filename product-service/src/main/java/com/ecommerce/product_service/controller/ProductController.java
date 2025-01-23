package com.ecommerce.product_service.controller;

import com.ecommerce.product_service.config.onCreate;
import com.ecommerce.product_service.config.onUpdate;
import com.ecommerce.product_service.dto.ProductDTO;
import com.ecommerce.product_service.dto.ResponseDTO;
import com.ecommerce.product_service.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("view")
    public ResponseEntity<ResponseDTO> fetchAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("view/{product}")
    public ResponseEntity<ResponseDTO> getProductDetailsById(@PathVariable String product) {
        return ResponseEntity.ok(productService.getProductByProductName(product));
    }

    @PostMapping("add")
    public ResponseEntity<ResponseDTO> addProductDetails(@RequestBody @Validated(onCreate.class) ProductDTO productDTO) {
        return new ResponseEntity<>(productService.addProduct(productDTO), HttpStatus.CREATED);
    }

    @PutMapping("edit")
    public ResponseEntity<ResponseDTO> editProductDetails(@RequestBody @Validated(onUpdate.class) ProductDTO productDTO) {
        return new ResponseEntity<>(productService.editProduct(productDTO), HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("delete/{product}")
    public ResponseEntity<Object> deleteProductDetails(@PathVariable String product) {
        productService.deleteProduct(product);
        return ResponseEntity.noContent().build();
    }

}
