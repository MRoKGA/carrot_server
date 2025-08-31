package com.mrokga.carrot_server.repository;

import com.mrokga.carrot_server.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Integer> {
}
