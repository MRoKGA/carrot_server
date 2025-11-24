package com.mrokga.carrot_server.group.repository;

import com.mrokga.carrot_server.group.entity.GroupEventRsvp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GroupEventRsvpRepository extends JpaRepository<GroupEventRsvp, Integer> {
    Optional<GroupEventRsvp> findByEventIdAndUserId(Integer eventId, Integer userId);
    long countByEventIdAndStatus(Integer eventId, GroupEventRsvp.Status status);
}
