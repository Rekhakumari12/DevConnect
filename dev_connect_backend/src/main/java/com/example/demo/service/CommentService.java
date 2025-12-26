package com.example.demo.service;

import com.example.demo.dto.CommentResponse;
import com.example.demo.entity.Comment;
import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import com.example.demo.enums.PostVisibility;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.ReactionRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.AuthUtil;
import com.example.demo.utils.ReactionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;

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
        CommentResponse c = new CommentResponse();
        c.setId(comment.getId());
        c.setContent(comment.getContent());
        c.setUsername(comment.getUser().getUsername());
        c.setReactions(reactionMapper.toReactionMap(comment.getReactions()));
        return c;
    }

     public CommentResponse addComment(UUID postId, String content, String username) {
         User user = userRepo.findByUsername(username).orElseThrow();
         Post post = postRepo.findById(postId).orElseThrow();

         if(post.getVisibility()!= PostVisibility.PUBLIC){
             throw new AccessDeniedException("Cannot comment on a private post.");
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
         authUtil.verifyUserAccess(c.getUser().getUsername());
         commentRepo.delete(c);
     }

     public List<CommentResponse> getCommentByPostId(UUID postId) {
        postRepo.findById(postId)
                .orElseThrow(()->new ResourceAccessException("Post not found"));

        List<Comment> comments = commentRepo.findAllByPostId(postId);
        return comments.stream().map(comment -> {
            CommentResponse commentResponse = new CommentResponse();
            commentResponse.setId(comment.getId());
            commentResponse.setContent(comment.getContent());
            commentResponse.setUsername(comment.getUser().getUsername());
            commentResponse.setReactions(reactionMapper.toReactionMap(comment.getReactions()));
            return commentResponse;
        }).toList();
     }
}
