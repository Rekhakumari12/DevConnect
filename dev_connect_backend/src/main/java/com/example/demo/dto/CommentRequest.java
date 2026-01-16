package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;

public record CommentRequest(
        @NotBlank
        String content
) {}
