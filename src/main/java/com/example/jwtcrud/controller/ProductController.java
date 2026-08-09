package com.example.jwtcrud.controller;

import com.example.jwtcrud.dto.MessageResponse;
import com.example.jwtcrud.dto.ProductRequest;
import com.example.jwtcrud.model.Product;
import com.example.jwtcrud.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Every endpoint here is protected by SecurityConfig (anyRequest().authenticated()).
// A request without a valid "Authorization: Bearer <token>" header gets a 401/403.
// Chetan Changes in Product controller
// Chetan Second changes.
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    //This is api is used tp create product
    @PostMapping
    public ResponseEntity<Product> create(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAll() {
        return ResponseEntity.ok(productService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.ok(new MessageResponse("Product deleted successfully"));
    }
}
