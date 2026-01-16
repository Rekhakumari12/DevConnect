package com.example.demo.dto;

import com.example.demo.entity.Comment;
import com.example.demo.utils.ReactionMapper;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record CommentResponse(
        UUID id,
        String content,
        String username,
        List<ReactionSummary> reactions
) {}
