package com.example.demo.service;

import com.example.demo.entity.Comment;
import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import com.example.demo.enums.PostVisibility;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.security.Principal;
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

     public Comment addComment(UUID postId, String content, Principal principal) {
         String username = principal.getName();
         User user = userRepo.findByUsername(username).orElseThrow();
         Post post = postRepo.findById(postId).orElseThrow();

         if(post.getVisibility()!= PostVisibility.PUBLIC){
             throw new AccessDeniedException("Cannot comment on a private post.");
         }

        Comment c = new Comment();
        c.setContent(content);
        c.setPost(post);
        c.setUser(user);

        return commentRepo.save(c);
     }

     public void deleteComment(UUID commentId) {
         Comment c = commentRepo.findById(commentId).orElseThrow();
         authUtil.verifyUserAccess(c.getUser().getUsername());
         commentRepo.delete(c);
     }


}
