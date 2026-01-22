package com.example.demo.dto;

import java.time.LocalDateTime;
import java.util.List;
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
         long commentCount,
         long reactionCount
) {}


