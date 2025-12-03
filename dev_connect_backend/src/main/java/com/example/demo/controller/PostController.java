package com.example.demo.controller;

import com.example.demo.dto.PostRequest;
import com.example.demo.dto.PostResponse;
import com.example.demo.entity.Post;
import com.example.demo.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/posts")
public class PostController {

    @Autowired
    private PostService postService;


    @GetMapping("/{username}")
    public ResponseEntity<List<PostResponse>> getPostsByUsername(@PathVariable String username) {
        List<PostResponse> posts = postService.getPostsByUsername(username);
        return ResponseEntity.ok(posts);
    }


    @PostMapping("/create")
    public ResponseEntity<PostResponse> createPost(@RequestBody PostRequest post, Principal principal) {
        String username = principal.getName();
        PostResponse postResponse = postService.createPost(post, username);
        return ResponseEntity.ok(postResponse);
    }

    @PutMapping("/{postId}")
    public ResponseEntity<PostResponse> updatePost(@RequestBody PostRequest req, @PathVariable UUID postId, Principal principal) {
        String username = principal.getName();
        PostResponse post = postService.updatePost(req, username, postId);
        return ResponseEntity.ok(post);
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable UUID postId, Principal principal) {
        String username = principal.getName();
        postService.deletePost(postId, username);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/all")
    public ResponseEntity<List<PostResponse>> getAllPosts() {
        List<PostResponse> posts = postService.getPublicPosts();
        return ResponseEntity.ok(posts);
    }

}
