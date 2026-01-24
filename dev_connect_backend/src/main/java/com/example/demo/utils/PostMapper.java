package com.example.demo.utils;

import com.example.demo.dto.post.PostResponse;
import com.example.demo.entity.Post;
import com.example.demo.service.CommentService;
import com.example.demo.service.reaction.ReactionService;
import org.springframework.stereotype.Component;

@Component
public class PostMapper {

    private final CommentService commentService;
    private final ReactionService reactionService;

    public PostMapper(CommentService commentService, ReactionService reactionService) {
        this.commentService = commentService;
        this.reactionService = reactionService;
    }

    public PostResponse toResponse(Post post) {
        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getTags(),
                post.getVisibility().name(),
                post.getUser().getUsername(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                commentService.getCountByPostId(post.getId()),
                reactionService.getCountByPostId(post.getId())
        );
    }
}

