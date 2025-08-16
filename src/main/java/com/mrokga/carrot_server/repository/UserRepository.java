package com.mrokga.carrot_server.repository;

import com.mrokga.carrot_server.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    User findByNickname(String nickname);
}
