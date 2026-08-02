package com.example.jwtcrud.repository;

import com.example.jwtcrud.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
