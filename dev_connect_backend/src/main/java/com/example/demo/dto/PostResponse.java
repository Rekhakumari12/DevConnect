package com.example.demo.dto;

import com.example.demo.entity.Reaction;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record PostResponse(
         UUID id,
         String title,
         String content,
         List<String> tags,
         String visibility,
         String username,
         LocalDateTime createdAt,
         LocalDateTime updatedAt,
         List<CommentResponse> comments,
         List<ReactionSummary> reactions
) {}


