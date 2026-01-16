package com.example.demo.controller;

import com.example.demo.dto.PostRequest;
import com.example.demo.dto.PostResponse;
import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import com.example.demo.model.UserPrincipal;
import com.example.demo.security.AuthUtil;
import com.example.demo.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/posts")
public class PostController {

    @Autowired
    private PostService postService;

    @PostMapping("")
    public ResponseEntity<PostResponse> createPost(@RequestBody PostRequest post, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        PostResponse postResponse = postService.createPost(post, userPrincipal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(postResponse);
    }

    @GetMapping("/all")
    public ResponseEntity<List<PostResponse>> getAllPost() {
        List<PostResponse> posts = postService.getPublicPosts();
        return ResponseEntity.ok(posts);
    }

    @GetMapping("")
    public ResponseEntity<List<PostResponse>> getPosts(@RequestParam(required = false) UUID userId, @AuthenticationPrincipal UserPrincipal principal) {
        List<PostResponse> posts;
        if (userId == null) { // get current logged in user posts
            posts =postService.getPostsByUserId(principal.getId());
        }else {
            posts = postService.getPostsByUserId(userId);
        }
        return ResponseEntity.ok(posts);
    }

    @PutMapping("/{postId}")
    public ResponseEntity<PostResponse> updatePost(@RequestBody PostRequest req, @PathVariable UUID postId, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        PostResponse post = postService.updatePost(req, userPrincipal.getId(), postId);
        return ResponseEntity.ok(post);
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable UUID postId, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        postService.deletePost(postId, userPrincipal.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<Page<PostResponse>> searchPosts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<PostResponse> posts = postService.searchPosts(keyword, page, size);
        return ResponseEntity.ok(posts);
    }
}
