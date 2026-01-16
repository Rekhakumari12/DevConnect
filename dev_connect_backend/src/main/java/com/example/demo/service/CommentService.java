package com.example.demo.service;

import com.example.demo.dto.CommentResponse;
import com.example.demo.entity.Comment;
import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import com.example.demo.enums.PostVisibility;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.ReactionRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.AuthUtil;
import com.example.demo.utils.ReactionMapper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.UUID;

@Service
public class CommentService {

    @Autowired
    UserRepository userRepo;

    @Autowired
    PostRepository postRepo;

    @Autowired
    CommentRepository commentRepo;

    @Autowired
    AuthUtil authUtil;

    @Autowired
    ReactionRepository reactionRepo;

    @Autowired
    private ReactionMapper reactionMapper;

    private CommentResponse toResponse(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getUser().getUsername(),
                reactionMapper.toReactionMap(comment.getReactions())
        );
    }

     public CommentResponse addComment(UUID postId, String content, String username) {
         User user = userRepo.findByUsername(username).orElseThrow();
         Post post = postRepo.findById(postId).orElseThrow();

         if(post.getVisibility()!= PostVisibility.PUBLIC){
             throw new AccessDeniedException("cannot comment on a private post.");
         }

        Comment c = new Comment();
        c.setContent(content);
        c.setPost(post);
        c.setUser(user);

        Comment saved = commentRepo.save(c);
        return toResponse(saved);
     }

     public void deleteComment(UUID commentId) {
         Comment c = commentRepo.findById(commentId).orElseThrow();
         authUtil.verifyUserAccess(c.getUser().getId());
         commentRepo.delete(c);
     }

     public List<CommentResponse> getCommentByPostId(UUID postId) {
        postRepo.findById(postId)
                .orElseThrow(()-> new ResourceNotFoundException("post not found"));

        List<Comment> comments = commentRepo.findAllByPostId(postId);
        return comments.stream().map(this::toResponse).toList();
     }
}
