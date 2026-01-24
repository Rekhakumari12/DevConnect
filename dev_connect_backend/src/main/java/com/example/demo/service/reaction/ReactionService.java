package com.example.demo.service.reaction;

import com.example.demo.dto.reaction.ReactionResponse;
import com.example.demo.dto.reaction.ReactionResponseList;
import com.example.demo.dto.reaction.ReactionSummary;
import com.example.demo.entity.Comment;
import com.example.demo.entity.Post;
import com.example.demo.entity.Reaction;
import com.example.demo.entity.User;
import com.example.demo.enums.ReactionType;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.ReactionRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.CommentService;
import com.example.demo.service.UserService;
import com.example.demo.service.post.PostService;
import com.example.demo.utils.ReactionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ReactionService {

    private final ReactionRepository reactionRepo;
    private final ReactionMapper reactionMapper;
    private final CommentService commentService;
    private final UserService userService;
    private final PostService postService;

    @Autowired
    public ReactionService(
            PostRepository postRepo,
            ReactionRepository reactionRepo,
            ReactionMapper reactionMapper,
            CommentService commentService,
            UserService userService,
            PostService postService
    ) {
        this.reactionRepo = reactionRepo;
        this.reactionMapper = reactionMapper;
        this.commentService = commentService;
        this.userService = userService;
        this.postService = postService;
    }

    private ReactionResponseList buildResponseList(List<Reaction> reactions) {
        List<ReactionSummary> reactionMap = reactionMapper.toReactionMap(reactions);
        return new ReactionResponseList(reactionMap);
    }

    private ReactionResponse buildResponse(
            ReactionType type,
            UUID userId,
            ReactionTarget target
    ) {
        return new ReactionResponse(
                type,
                userId,
                new ReactionResponse.Target(
                        target.getTargetId(),
                        target.getType()
                )
        );
    }

    // target - post or comment
    private ReactionResponse react(
            ReactionType type,
            ReactionTarget target,
            UUID userId,
            Optional<Reaction> currentReaction
    ) {
        User user = userService.getById(userId);

        // undo existing reaction
        if (currentReaction.isPresent()
                && currentReaction.get().getType() == type) {

            reactionRepo.delete(currentReaction.get());
            return buildResponse(null, userId, target);
        }

        Reaction reaction = currentReaction.orElseGet(Reaction::new);
        reaction.setType(type);
        reaction.setUser(user);

        if(reaction.getId() == null) {
            target.linkReaction(reaction);
        }

        Reaction saved = reactionRepo.save(reaction);
        return buildResponse(saved.getType(), userId, target);
    }


    public ReactionResponseList getReactionsByPostId(UUID postId) {
        postService.getById(postId);
        List<Reaction> reactions = reactionRepo.findAllByPostId(postId);
        return buildResponseList(reactions);
    }

    public ReactionResponseList getReactionsByCommentId(UUID commentId) {
        commentService.getById(commentId);
        List<Reaction> reactions = reactionRepo.findAllByCommentId(commentId);
        return buildResponseList(reactions);
    }

    public ReactionResponse reactToPost(UUID postId, ReactionType type, UUID userId) {
        Post post = postService.getById(postId);
        Optional<Reaction> currentReaction = reactionRepo.findByUserIdAndPostId(userId, postId);
        ReactionTarget target = new PostReaction(post);
        return react(type, target, userId, currentReaction);
    }

    public ReactionResponse reactToComment(UUID commentId, ReactionType type, UUID userId){
        Comment comment = commentService.getById(commentId);
        Optional<Reaction> currentReaction = reactionRepo.findByUserIdAndCommentId(userId, commentId);
        ReactionTarget target = new CommentReaction(comment);
        return react(type, target, userId, currentReaction);
    }

    public long getCountByPostId(UUID postId) {
        return reactionRepo.countByPostId(postId);
    }

    public long getCountByCommentId(UUID commentId) {
        return reactionRepo.countByCommentId(commentId);
    }
}
