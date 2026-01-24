package com.example.demo.service.reaction;

import com.example.demo.entity.Reaction;
import com.example.demo.enums.ReactionTargetType;

import java.util.Optional;
import java.util.UUID;

public interface ReactionTarget {
    void linkReaction(Reaction reaction);
    UUID getTargetId();
    ReactionTargetType getType();
}
