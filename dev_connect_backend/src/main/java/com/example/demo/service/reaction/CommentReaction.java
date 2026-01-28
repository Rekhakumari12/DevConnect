package com.example.demo.service.reaction;

import com.example.demo.entity.Comment;
import com.example.demo.entity.Reaction;
import com.example.demo.enums.ReactionTargetType;

import java.util.UUID;

public record CommentReaction(Comment comment) implements ReactionTarget {

    @Override
    public void linkReaction(Reaction reaction) {
        reaction.setComment(comment);
    }

    @Override
    public UUID getTargetId() {
        return comment.getId();
    }

    @Override
    public ReactionTargetType getType() {
        return ReactionTargetType.COMMENT;
    }
}
