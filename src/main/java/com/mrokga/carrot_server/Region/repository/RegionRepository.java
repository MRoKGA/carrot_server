package com.mrokga.carrot_server.Region.repository;

import com.mrokga.carrot_server.Region.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RegionRepository extends JpaRepository<Region, Integer> {
    Optional<Region> findByFullName(String fullName);
    Optional<Region> findByName(String name);
}
