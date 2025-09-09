package com.mrokga.carrot_server.repository;

import com.mrokga.carrot_server.entity.QuickReply;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuickReplyRepository extends JpaRepository<QuickReply, Integer> {
    Optional<QuickReply> findByUserIdAndBodyNorm(Integer ownerId, String bodyNorm);

    List<QuickReply> findTop100ByUserIdOrderByCreatedAtDesc(Integer ownerId);

    long deleteByUserIdAndId(Integer ownerId, Integer id);

    boolean existsByUserIdAndBodyNorm(Integer ownerId, String bodyNorm);
}
