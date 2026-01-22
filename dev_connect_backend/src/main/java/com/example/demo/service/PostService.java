package com.example.demo.service;

import com.example.demo.dto.PostRequest;
import com.example.demo.dto.PostResponse;
import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import com.example.demo.enums.PostVisibility;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.AuthUtil;
import com.example.demo.service.fetch.LoggedInPostFetchStrategy;
import com.example.demo.service.fetch.PostFetchStrategy;
import com.example.demo.service.fetch.PublicPostFetchStrategy;
import com.example.demo.utils.PostMapper;
import com.example.demo.utils.ReactionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PostService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private PostRepository postRepo;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private CommentService commentService;

    @Autowired
    private ReactionMapper reactionMapper;

    @Autowired
    private PostMapper postMapper;


    public PostResponse createPost(PostRequest postRequest, UUID userId) {
        User user = userRepo.findById(userId);
        Post post = new Post();
        post.setUser(user);
        post.setTitle(postRequest.title());
        post.setContent(postRequest.content());
        post.setTags(postRequest.techStack());
        post.setVisibility(postRequest.visibility());
        Post saved = postRepo.save(post);
        return postMapper.toResponse(saved);
    }

    public List<PostResponse> getPublicPosts() {
        return postRepo.findByVisibility(PostVisibility.PUBLIC)
                .stream()
                .map(postMapper::toResponse)
                .toList();
    }

    public List<PostResponse> getPostsByUsername(String username) {
        PostFetchStrategy strategy =
                AuthUtil.isAuthenticated()
                        ? new LoggedInPostFetchStrategy(postRepo)
                        : new PublicPostFetchStrategy(postRepo);

        return strategy.fetchPosts(username)
                .stream()
                .map(postMapper::toResponse)
                .toList();
    }

    public PostResponse updatePost(PostRequest req, String username, UUID postId) {
        Post post = getOwnedPost(username, postId);
        if(req.title() != null) post.setTitle(req.title());
        if(req.content() != null) post.setContent(req.content());
        if(req.techStack() != null) post.setTags(req.techStack());
        if(req.visibility() != null) post.setVisibility(req.visibility());
        Post updatedPost = postRepo.save(post);
        return postMapper.toResponse(updatedPost);
    }

    public void deletePost(UUID postId, String username) {
        Post post = getOwnedPost(username, postId);
        postRepo.delete(post);
    }

    private Post getOwnedPost(String username, UUID postId) {
        return postRepo.findByIdAndUser_Username(postId, username)
                .orElseThrow(() -> new AccessDeniedException("Access Denied"));
    }

}
