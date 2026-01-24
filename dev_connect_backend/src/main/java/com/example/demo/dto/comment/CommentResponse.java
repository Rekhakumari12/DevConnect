package com.example.demo.dto.comment;

import java.util.UUID;

public record CommentResponse(
        UUID id,
        String content,
        String username,
        long reactionsCount
) {}
