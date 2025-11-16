package com.mrokga.carrot_server.community.repository;

import com.mrokga.carrot_server.community.entity.PostCategory;
import com.mrokga.carrot_server.community.entity.Post;
import com.mrokga.carrot_server.region.entity.Region;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Integer> {
    /**
     * 지역은 필수, 카테고리는 nullable
     * 하나의 메서드로 지역만 필터 / 지역+카테고리 필터
     */
    @Query("""
        SELECT p FROM Post p
        WHERE p.region = :region
          AND (:category IS NULL OR p.postCategory = :category)
          AND (:keyword IS NULL\s
                             OR REPLACE(LOWER(p.title), ' ', '')\s
                                LIKE LOWER(CONCAT('%', REPLACE(:keyword, ' ', ''), '%')))
        ORDER BY p.createdAt DESC
    """)
    Page<Post> findByRegionAndOptionalCategoryAndKeyword(
            @Param("region") Region region,
            @Param("category") PostCategory category,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
