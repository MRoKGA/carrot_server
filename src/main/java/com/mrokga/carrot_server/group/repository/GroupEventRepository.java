package com.mrokga.carrot_server.group.repository;

import com.mrokga.carrot_server.group.entity.GroupEvent;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface GroupEventRepository extends JpaRepository<GroupEvent, Integer> {
    Page<GroupEvent> findByGroupIdAndStartAtAfterOrderByStartAtAsc(Integer groupId, LocalDateTime now, Pageable pageable);
}
