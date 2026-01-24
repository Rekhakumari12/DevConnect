package com.example.demo.service.post;

import com.example.demo.enums.PostVisibility;
import com.example.demo.repository.PostRepository;
import com.example.demo.entity.Post;

import java.util.List;

public record PublicPostFetchStrategy(PostRepository postRepo) implements PostFetchStrategy {

    @Override
    public List<Post> fetchPosts(String username) {
        return postRepo.findAllByUser_Username(username)
                .stream()
                .filter(p -> p.getVisibility() == PostVisibility.PUBLIC)
                .toList();
    }
}
