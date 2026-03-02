package com.mrokga.carrot_server.group.repository;

import com.mrokga.carrot_server.group.entity.GroupJoinRequest;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupJoinRequestRepository extends JpaRepository<GroupJoinRequest, Integer> {
    @EntityGraph(attributePaths = {"user", "group"})
    Page<GroupJoinRequest> findByGroupIdAndStatus(Integer groupId,
                                                  GroupJoinRequest.Status status, Pageable pageable);
}
