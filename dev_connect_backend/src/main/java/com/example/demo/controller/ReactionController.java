package com.example.demo.controller;

import com.example.demo.dto.reaction.ReactionRequest;
import com.example.demo.dto.reaction.ReactionResponse;
import com.example.demo.dto.reaction.ReactionSummary;
import com.example.demo.model.UserPrincipal;
import com.example.demo.service.reaction.ReactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class ReactionController {

    @Autowired
    private ReactionService reactionService;

    @PostMapping("/posts/{postId}/reactions")
    public ResponseEntity<ReactionResponse> reactToPost(@PathVariable UUID postId, @RequestBody ReactionRequest req, @AuthenticationPrincipal UserPrincipal principal) {
        ReactionResponse reaction = reactionService.reactToPost(postId, req.type(), principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(reaction);
    }

    @PostMapping("/comments/{commentId}/reactions")
    public ResponseEntity<ReactionResponse> reactToComment(@PathVariable UUID commentId, @RequestBody ReactionRequest req, @AuthenticationPrincipal UserPrincipal principal) {
        ReactionResponse reaction = reactionService.reactToComment(commentId, req.type(), principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(reaction);
    }

    @GetMapping("/posts/{postId}/reactions")
    public ResponseEntity<List<ReactionSummary>> getPostReactions(@PathVariable UUID postId) {
        List<ReactionSummary> reaction = reactionService.getReactionsByPostId(postId);
        return ResponseEntity.ok(reaction);
    }

    @GetMapping("/comments/{commentId}/reactions")
    public ResponseEntity<List<ReactionSummary>> getCommentReactions(@PathVariable UUID commentId) {
        List<ReactionSummary> reaction = reactionService.getReactionsByCommentId(commentId);
        return ResponseEntity.ok(reaction);
    }
}
