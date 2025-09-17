package com.mrokga.carrot_server.Product.repository;

import com.mrokga.carrot_server.Product.entity.ProductExposureRegion;
import com.mrokga.carrot_server.Region.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductExposureRegionRepository extends JpaRepository<ProductExposureRegion, Integer> {
    List<ProductExposureRegion> findAllByRegion(Region region);
}
