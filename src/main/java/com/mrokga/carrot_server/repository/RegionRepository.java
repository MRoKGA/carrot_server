package com.mrokga.carrot_server.repository;

import com.mrokga.carrot_server.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegionRepository extends JpaRepository<Region, Integer> {
    Region findByFullName(String fullName);
}
