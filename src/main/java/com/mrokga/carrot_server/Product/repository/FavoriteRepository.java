package com.mrokga.carrot_server.Product.repository;

import com.mrokga.carrot_server.Product.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Integer> {
    public Favorite findByUserIdAndProductId(Integer userId, Integer productId);
}
