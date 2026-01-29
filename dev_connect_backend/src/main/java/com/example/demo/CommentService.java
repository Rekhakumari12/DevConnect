package com.example.demo;

import com.example.demo.dto.comment.CommentResponse;
import com.example.demo.entity.Comment;
import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import com.example.demo.enums.PostVisibility;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.CommentRepository;
import com.example.demo.security.AuthUtil;
import com.example.demo.post.PostService;
import com.example.demo.reaction.ReactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CommentService {
    private final UserService userService;
    private final PostService postService;
    private final CommentRepository commentRepo;
    private final AuthUtil authUtil;
    private final ReactionService reactionService;

    @Autowired
    public CommentService(UserService userService,
                          PostService postService,
                          CommentRepository commentRepo,
                          AuthUtil authUtil,
                          ReactionService reactionService) {

        this.userService = userService;
        this.postService = postService;
        this.commentRepo = commentRepo;
        this.authUtil = authUtil;
        this.reactionService = reactionService;
    }

    private CommentResponse toResponse(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getUser().getUsername(),
                reactionService.getCountByCommentId(comment.getId())
        );
    }

     public CommentResponse addComment(UUID postId, String content, String username) {
         User user = userService.getByUsername(username);
         Post post = postService.getById(postId);

         if(post.getVisibility()!= PostVisibility.PUBLIC){
             throw new AccessDeniedException("Access Denied");
         }

        Comment c = new Comment();
        c.setContent(content);
        c.setPost(post);
        c.setUser(user);

        Comment saved = commentRepo.save(c);
        return toResponse(saved);
     }

     public void deleteComment(UUID commentId) {
         Comment c = getById(commentId);
         authUtil.verifyUserAccess(c.getUser().getUsername());
         commentRepo.delete(c);
     }

     public List<CommentResponse> getCommentByPostId(UUID postId) {
        postService.getById(postId);
        postService.checkPrivatePost(postId);
        List<Comment> comments = commentRepo.findAllByPostId(postId);
        return comments.stream().map(this::toResponse).toList();
     }

     public long getCountByPostId(UUID postId) {
        return commentRepo.countByPostId(postId);
     }

     public Comment getById(UUID commentId) {
        return commentRepo.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
     }
}
