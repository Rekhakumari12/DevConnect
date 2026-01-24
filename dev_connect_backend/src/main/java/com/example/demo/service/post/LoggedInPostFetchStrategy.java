package com.example.demo.service.post;

import com.example.demo.entity.Post;
import com.example.demo.repository.PostRepository;

import java.util.List;

public record LoggedInPostFetchStrategy(PostRepository postRepo) implements PostFetchStrategy {

    @Override
    public List<Post> fetchPosts(String username) {
        return postRepo.findAllByUser_Username(username);
    }
}
