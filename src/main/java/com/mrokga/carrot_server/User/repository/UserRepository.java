package com.mrokga.carrot_server.User.repository;

import com.mrokga.carrot_server.User.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    User findByNickname(String nickname);
    User findByPhoneNumber(String phoneNumber);
}
