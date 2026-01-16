package com.example.demo.dto;

import com.example.demo.enums.ReactionType;
import jakarta.validation.constraints.NotBlank;

public record ReactionRequest(
        @NotBlank
        ReactionType type
) {}
