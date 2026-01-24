package com.example.demo.service.post;
import com.example.demo.entity.Post;
import java.util.List;

public interface PostFetchStrategy {
    List<Post> fetchPosts(String username);
}
