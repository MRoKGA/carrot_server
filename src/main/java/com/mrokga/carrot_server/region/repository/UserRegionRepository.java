package com.mrokga.carrot_server.region.repository;

import com.mrokga.carrot_server.region.entity.UserRegion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRegionRepository extends JpaRepository<UserRegion, Long> {

    // 활성화된 동네(필요시)
    Optional<UserRegion> findActiveByUserId(Integer userId);

    // ✅ 현재 대표 동네
    @Query("""
           select ur
             from UserRegion ur
            where ur.user.id = :userId and ur.isPrimary = true
           """)
    Optional<UserRegion> findPrimaryByUserId(@Param("userId") Integer userId);

    // ✅ 사용자 + 지역 매핑 존재 여부
    Optional<UserRegion> findByUserIdAndRegion_Id(Integer userId, Integer regionId);

    // 목록 조회 (지역 join 포함)
    @Query("""
           select ur
             from UserRegion ur
             join fetch ur.region r
            where ur.user.id = :userId
            order by ur.isPrimary desc, ur.createdAt desc
           """)
    List<UserRegion> findAllWithRegionByUserId(@Param("userId") Integer userId);
}
