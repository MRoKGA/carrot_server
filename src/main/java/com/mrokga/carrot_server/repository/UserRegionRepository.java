package com.mrokga.carrot_server.repository;

import com.mrokga.carrot_server.entity.UserRegion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRegionRepository extends JpaRepository<UserRegion, Long> {
    List<UserRegion> findAllByUserId(Integer userId);
}
