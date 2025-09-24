package com.mrokga.carrot_server.Product.repository;

import com.mrokga.carrot_server.Product.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
}
