package com.mrokga.carrot_server.group.repository;

import com.mrokga.carrot_server.group.entity.Group;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, Integer> {

    @EntityGraph(attributePaths = {"region", "owner"})
    Page<Group> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"region", "owner"})
    Page<Group> findByNameContainingIgnoreCase(String q, Pageable pageable);

    @EntityGraph(attributePaths = {"region", "owner"})
    Page<Group> findByRegionIdAndVisibility(Integer regionId, Group.Visibility visibility, Pageable pageable);


    // 👇 상세 조회 전용 (region, owner 미리 로딩)
    @EntityGraph(attributePaths = {"region", "owner"})
    Optional<Group> findWithOwnerAndRegionById(Integer id);
}
