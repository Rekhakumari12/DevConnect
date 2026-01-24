package com.example.demo.dto.reaction;

import com.example.demo.enums.ReactionTargetType;
import com.example.demo.enums.ReactionType;

import java.util.UUID;

public record ReactionResponse(
        ReactionType type,
        UUID userId,
        Target target
) {
    public record Target(
            UUID id,
            ReactionTargetType type
    ) {}
}
