package com.mrokga.carrot_server.product.repository;

import com.mrokga.carrot_server.product.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Integer> {
    Favorite findByUserIdAndProductId(Integer userId, Integer productId);

    Favorite findByUserIdAndCategoryId(Integer userId, Integer categoryId);
}
