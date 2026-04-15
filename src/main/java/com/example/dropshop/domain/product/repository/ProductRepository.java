package com.example.dropshop.domain.product.repository;

import com.example.dropshop.domain.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 상품 엔티티 저장소.
 */
public interface ProductRepository extends JpaRepository<Product, Long> {
}

