package com.example.demo.service.fetch;

import com.example.demo.enums.PostVisibility;
import com.example.demo.repository.PostRepository;
import com.example.demo.entity.Post;

import java.util.List;

public class PublicPostFetchStrategy implements PostFetchStrategy {
    private final PostRepository postRepo;

    public PublicPostFetchStrategy(PostRepository postRepo) {
        this.postRepo = postRepo;
    }

    @Override
    public List<Post> fetchPosts(String username) {
        return postRepo.findAllByUser_Username(username)
                .stream()
                .filter(p -> p.getVisibility() == PostVisibility.PUBLIC)
                .toList();
    }
}
