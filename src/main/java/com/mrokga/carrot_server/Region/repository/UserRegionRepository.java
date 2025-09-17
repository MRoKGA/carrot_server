package com.mrokga.carrot_server.Region.repository;

import com.mrokga.carrot_server.Region.entity.UserRegion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRegionRepository extends JpaRepository<UserRegion, Long> {

    Optional<UserRegion> findActiveByUserId(int userId);

    @Query("""
           select ur
             from UserRegion ur
             join fetch ur.region r
            where ur.user.id = :userId
            order by ur.isPrimary desc, ur.createdAt desc
           """)
    List<UserRegion> findAllWithRegionByUserId(@Param("userId") Integer userId);
}
