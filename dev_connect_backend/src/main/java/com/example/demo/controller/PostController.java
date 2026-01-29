package com.example.demo.controller;

import com.example.demo.dto.post.PostRequest;
import com.example.demo.dto.post.PostResponse;
import com.example.demo.model.UserPrincipal;
import com.example.demo.post.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/posts")
public class PostController {

    @Autowired
    private PostService postService;

    @PostMapping("")
    public ResponseEntity<PostResponse> createPost(@RequestBody PostRequest post, @AuthenticationPrincipal UserPrincipal p) {
        PostResponse postResponse = postService.createPost(post, p.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(postResponse);
    }

    @GetMapping("/my-post")
    public ResponseEntity<List<PostResponse>> getMyPosts(@AuthenticationPrincipal UserPrincipal p) {
        List<PostResponse> posts = postService.getPostsByUsername(p.getUsername());
        return ResponseEntity.ok(posts);
    }

    @GetMapping("")
    public ResponseEntity<List<PostResponse>> getPostsByUsername(@RequestParam String username) {
        List<PostResponse> posts = postService.getPostsByUsername(username);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/public")
    public ResponseEntity<Page<PostResponse>> getAllPost(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "5") int size) {
        Page<PostResponse> posts = postService.getPublicPosts(page, size);
        return ResponseEntity.ok(posts);
    }

    @PutMapping("/{postId}")
    public ResponseEntity<PostResponse> updatePost(@RequestBody PostRequest req, @PathVariable UUID postId, @AuthenticationPrincipal UserPrincipal p) {
        PostResponse post = postService.updatePost(req, p.getUsername(), postId);
        return ResponseEntity.ok(post);
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable UUID postId, @AuthenticationPrincipal UserPrincipal p) {
        postService.deletePost(postId, p.getUsername());
        return ResponseEntity.noContent().build();
    }
}
