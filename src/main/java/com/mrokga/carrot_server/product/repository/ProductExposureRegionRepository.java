package com.mrokga.carrot_server.product.repository;

import com.mrokga.carrot_server.product.entity.Product;
import com.mrokga.carrot_server.product.entity.ProductExposureRegion;
import com.mrokga.carrot_server.region.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductExposureRegionRepository extends JpaRepository<ProductExposureRegion, Integer> {
    List<ProductExposureRegion> findAllByRegion(Region region);

    void deleteByProduct(Product product);
}
