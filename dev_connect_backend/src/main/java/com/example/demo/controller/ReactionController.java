package com.example.demo.controller;

import com.example.demo.dto.ReactionRequest;
import com.example.demo.dto.ReactionResponse;
import com.example.demo.entity.Reaction;
import com.example.demo.service.ReactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api")
public class ReactionController {

    @Autowired
    private ReactionService reactionService;

    @PostMapping("/posts/{postId}/reactions")
    public ResponseEntity<ReactionResponse> reactToPost(@PathVariable UUID postId, @RequestBody ReactionRequest req) {
        ReactionResponse reaction = reactionService.reactToPost(postId, req.getType());
        return ResponseEntity.ok(reaction);
    }

    @PostMapping("/comments/{commentId}/reactions")
    public ResponseEntity<ReactionResponse> reactToComment(@PathVariable UUID commentId, @RequestBody ReactionRequest req) {
        ReactionResponse reaction = reactionService.reactToComment(commentId, req.getType());
        return ResponseEntity.ok(reaction);
    }

    @GetMapping("/posts/{postId}/reactions")
    public ResponseEntity<ReactionResponse> getPostReactions(@PathVariable UUID postId) {
        ReactionResponse reaction = reactionService.getPostReactions(postId);
        return ResponseEntity.ok(reaction);
    }

    @GetMapping("/comments/{commentId}/reactions")
    public ResponseEntity<ReactionResponse> getCommentReactions(@PathVariable UUID commentId) {
        ReactionResponse reaction = reactionService.getCommentReactions(commentId);
        return ResponseEntity.ok(reaction);
    }
}
