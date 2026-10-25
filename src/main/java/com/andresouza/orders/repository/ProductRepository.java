package com.andresouza.orders.repository;

import com.andresouza.orders.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByActiveTrueAndNameContainingIgnoreCase(String name, Pageable pageable);
}
