package com.example.demo.service.fetch;
import com.example.demo.entity.Post;
import java.util.List;

public interface PostFetchStrategy {
    List<Post> fetchPosts(String username);
}
