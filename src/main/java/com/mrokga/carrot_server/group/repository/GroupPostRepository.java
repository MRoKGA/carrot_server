package com.mrokga.carrot_server.group.repository;

import com.mrokga.carrot_server.group.entity.GroupPost;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupPostRepository extends JpaRepository<GroupPost, Integer> {
    Page<GroupPost> findByGroupIdOrderByIdDesc(Integer groupId, Pageable pageable);
}
