package com.example.demo.service;

import com.example.demo.dto.ReactionResponse;
import com.example.demo.dto.ReactionResponseList;
import com.example.demo.dto.ReactionSummary;
import com.example.demo.entity.Comment;
import com.example.demo.entity.Post;
import com.example.demo.entity.Reaction;
import com.example.demo.entity.User;
import com.example.demo.enums.ReactionType;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.ReactionRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.AuthUtil;
import com.example.demo.utils.ReactionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class ReactionService {

    @Autowired
    private PostRepository postRepo;

    @Autowired
    private ReactionRepository reactionRepo;

    @Autowired
    private ReactionMapper reactionMapper;

    @Autowired
    private CommentRepository commentRepo;

    @Autowired
    private UserRepository userRepo;

    private ReactionResponseList buildReactionResponse(List<Reaction> reactions) {
        List<ReactionSummary> reactionMap = reactionMapper.toReactionMap(reactions);
        return new ReactionResponseList(reactionMap);
    }

    private ReactionResponse react(ReactionType type, Post post, Comment comment, UUID userId) {
        Reaction finalReaction = null;
        User user = userRepo.findById(userId);

        Optional<Reaction> existing =
                (post != null)
                        ? reactionRepo.findByUserIdAndPostId(userId, post.getId())
                        : reactionRepo.findByUserIdAndCommentId(userId, comment.getId());

        if(existing.isPresent()) {
            Reaction r = existing.get();

            if(r.getType() == type) {
                reactionRepo.delete(r);
            } else {
                r.setType(type);
                finalReaction = reactionRepo.save(r);
            }
        } else {
            Reaction r = new Reaction();
            r.setType(type);
            r.setPost(post);
            r.setComment(comment);
            r.setUser(user);
            finalReaction = reactionRepo.save(r);
        }

        if (finalReaction == null) {
            return new ReactionResponse(
                    null,
                    userId,
                    post != null ? post.getId() : null,
                    comment != null ? comment.getId() : null
            );
        }

        return new ReactionResponse(
                finalReaction.getType(),
                userId,
                post != null ? post.getId() : null,
                comment != null ? comment.getId() : null
        );
    }

    public ReactionResponseList getReactionsByPostId(UUID postId) {
        postRepo.findById(postId).orElseThrow();
        List<Reaction> reactions = reactionRepo.findAllByPostId(postId);
        return buildReactionResponse(reactions);
    }

    public ReactionResponseList getReactionsByCommentId(UUID commentId) {
        commentRepo.findById(commentId).orElseThrow();
        List<Reaction> reactions = reactionRepo.findAllByPostId(commentId);
        return buildReactionResponse(reactions);
    }
    public ReactionResponse reactToPost(UUID postId, ReactionType type, UUID userId) {
        Post post = postRepo.findById(postId).orElseThrow();
        return react(type, post, null, userId);
    }
    public ReactionResponse reactToComment(UUID commentId, ReactionType type, UUID userId){
        Comment comment = commentRepo.findById(commentId).orElseThrow();
        return react(type, null, comment, userId);
    }
}
