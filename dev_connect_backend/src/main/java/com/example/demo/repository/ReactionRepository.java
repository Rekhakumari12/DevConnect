package com.example.demo.repository;

import com.example.demo.entity.Reaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReactionRepository extends JpaRepository<Reaction, UUID> {
    List<Reaction> findAllByPostId(UUID id);
    List<Reaction> findAllByCommentId(UUID id);
    Optional<Reaction> findByUserIdAndPostId(UUID userId, UUID postId);
    Optional<Reaction> findByUserIdAndCommentId(UUID userId, UUID commentId);
    long countByPostId(UUID postId);
    long countByCommentId(UUID commentId);
}
