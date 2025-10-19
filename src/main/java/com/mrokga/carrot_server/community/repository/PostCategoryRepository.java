package com.mrokga.carrot_server.community.repository;

import com.mrokga.carrot_server.community.entity.PostCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostCategoryRepository extends JpaRepository<PostCategory, Integer> {
    /**
     * 카테고리 이름으로 조회
     * - 게시글 생성 시 categoryId 대신 categoryName 으로 요청이 올 경우 사용 가능
     * - 카테고리 관리 기능(드롭다운에 띄울 때)에도 사용 가능
     */
    Optional<PostCategory> findByName(String name);
}
