package com.example.demo.service;

import com.example.demo.dto.ReactionResponse;
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


    private ReactionResponse buildReactionResponse(List<Reaction> reactions) {
        Map<String, Integer> reactionMap = reactionMapper.toReactionMap(reactions);
        ReactionResponse response = new ReactionResponse();
        response.setReactions(reactionMap);
        return response;
    }

    private ReactionResponse react(ReactionType type, Post post, Comment comment) {
        UUID userId = AuthUtil.getCurrentUserId();
        Optional<Reaction> existing;

        if(post!=null) {
            existing = reactionRepo.findByUserIdAndPostId(userId, post.getId());
        }else{
            existing = reactionRepo.findByUserIdAndCommentId(userId, comment.getId());
        }

        if(existing.isPresent()) {
            Reaction existingReaction = existing.get();

            if(existingReaction.getType() == type) {
                reactionRepo.delete(existingReaction);
            } else {
                existingReaction.setType(type);
                reactionRepo.save(existingReaction);
            }
        } else {
            User user = userRepo.findById(userId);
            Reaction r = new Reaction();
            r.setType(type);
            r.setPost(post);
            r.setComment(comment);
            r.setUser(user);
            reactionRepo.save(r);
        }

        List<Reaction> reactions = (post!=null)
                ? reactionRepo.findAllByPostId(post.getId())
                : reactionRepo.findAllByCommentId(comment.getId());

        return buildReactionResponse(reactions);
    }

    public ReactionResponse getPostReactions(UUID postId) {
        postRepo.findById(postId).orElseThrow();
        List<Reaction> reactions = reactionRepo.findAllByPostId(postId);
        return buildReactionResponse(reactions);
    }

    public ReactionResponse getCommentReactions(UUID commentId) {
        commentRepo.findById(commentId).orElseThrow();
        List<Reaction> reactions = reactionRepo.findAllByPostId(commentId);
        return buildReactionResponse(reactions);
    }
    public ReactionResponse reactToPost(UUID postId, ReactionType type) {
        Post post = postRepo.findById(postId).orElseThrow();
        return react(type, post, null);
    }
    public ReactionResponse reactToComment(UUID commentId, ReactionType type){
        Comment comment = commentRepo.findById(commentId).orElseThrow();
        return react(type, null, comment);
    }
}
