package com.example.demo.reaction;

import com.example.demo.entity.Post;
import com.example.demo.entity.Reaction;
import com.example.demo.enums.ReactionTargetType;

import java.util.UUID;

public record PostReaction(Post post) implements ReactionTarget {

    @Override
    public void linkReaction(Reaction reaction) {
        reaction.setPost(post);
    }

    @Override
    public UUID getTargetId() {
        return post.getId();
    }

    @Override
    public ReactionTargetType getType() {
        return ReactionTargetType.POST;
    }
}
