package com.mrokga.carrot_server.repository;

import com.mrokga.carrot_server.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RegionRepository extends JpaRepository<Region, Integer> {
    Region findByFullName(String fullName);
    Optional<Region> findByName(String name);
}
