package com.example.demo.service.fetch;

import com.example.demo.entity.Post;
import com.example.demo.repository.PostRepository;

import java.util.List;

public class LoggedInPostFetchStrategy implements PostFetchStrategy {
    private final PostRepository postRepo;

    public LoggedInPostFetchStrategy(PostRepository postRepo) {
        this.postRepo = postRepo;
    }

    @Override
    public List<Post> fetchPosts(String username) {
        return postRepo.findAllByUser_Username(username);
    }
}
