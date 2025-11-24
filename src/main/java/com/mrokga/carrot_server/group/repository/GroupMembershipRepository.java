package com.mrokga.carrot_server.group.repository;

import com.mrokga.carrot_server.group.entity.GroupMembership;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GroupMembershipRepository extends JpaRepository<GroupMembership, Integer> {
    boolean existsByGroupIdAndUserId(Integer groupId, Integer userId);
    Optional<GroupMembership> findByGroupIdAndUserId(Integer groupId, Integer userId);
    long countByGroupIdAndIsActiveTrue(Integer groupId);
}
