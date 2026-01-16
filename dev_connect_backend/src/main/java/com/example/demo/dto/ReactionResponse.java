package com.example.demo.dto;

import com.example.demo.enums.ReactionType;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record ReactionResponse(
        ReactionType type,
        UUID userId,
        UUID postId,
        UUID commentId
) {}
