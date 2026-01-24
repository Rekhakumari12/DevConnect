package com.example.demo.dto.reaction;

import com.example.demo.enums.ReactionType;
import jakarta.validation.constraints.NotBlank;

public record ReactionRequest(
        @NotBlank
        ReactionType type
) {}
