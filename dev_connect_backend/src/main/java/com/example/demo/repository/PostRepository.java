package com.example.demo.repository;

import com.example.demo.entity.Post;
import com.example.demo.enums.PostVisibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<Post, Integer> {
    @Query("""
    SELECT DISTINCT p FROM Post p
    LEFT JOIN p.tags t
    WHERE
        LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(t) LIKE LOWER(CONCAT('%', :keyword, '%'))
""")
    Page<Post> searchPosts(String keyword, Pageable pageable);

    @Query("""
    SELECT DISTINCT p FROM Post p
    LEFT JOIN p.tags t
    WHERE p.visibility = 'PUBLIC'
    AND (
        LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(t) LIKE LOWER(CONCAT('%', :keyword, '%'))
    )
""")
    Page<Post> searchPublicPosts(String keyword, Pageable pageable);

    List<Post> findByVisibility(PostVisibility visibility);
    Optional<Post> findByIdAndUser_Username(UUID postId, String username);
    Optional<Post> findById(UUID id);
    List<Post> findAllByUser_Username(String username);

}
