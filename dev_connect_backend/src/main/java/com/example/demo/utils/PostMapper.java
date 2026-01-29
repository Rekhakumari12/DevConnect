package com.example.demo.utils;

import com.example.demo.dto.post.PostResponse;
import com.example.demo.entity.Post;
import org.springframework.stereotype.Component;

@Component
public class PostMapper {

    public PostResponse toResponse(
            Post post,
            long commentCount,
            long reactionCount
    ) {
        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getTags(),
                post.getVisibility().name(),
                post.getUser().getUsername(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                commentCount,
                reactionCount
        );
    }
}

