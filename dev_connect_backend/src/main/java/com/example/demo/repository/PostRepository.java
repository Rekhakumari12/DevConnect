package com.example.demo.repository;

import com.example.demo.entity.Post;
import com.example.demo.enums.PostVisibility;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, Integer> {
    List<Post> findByVisibility(PostVisibility visibility);
    Optional<Post> findByIdAndUser_Username(UUID postId, String username);
    List<Post> findAllByUser_Username(String username);
}
