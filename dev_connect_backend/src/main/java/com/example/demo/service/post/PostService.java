package com.example.demo.service.post;

import com.example.demo.dto.post.PostRequest;
import com.example.demo.dto.post.PostResponse;
import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import com.example.demo.enums.PostVisibility;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.ReactionRepository;
import com.example.demo.security.AuthUtil;
import com.example.demo.service.UserService;
import com.example.demo.utils.PostMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PostService {

    private final UserService userService;
    private final PostRepository postRepo;
    private final PostMapper postMapper;
    private final CommentRepository commentRepo;
    private final ReactionRepository reactionRepo;

    @Autowired
    public PostService(
            UserService userService,
            PostRepository postRepo,
            PostMapper postMapper,
            CommentRepository commentRepo,
            ReactionRepository reactionRepo
    ) {
        this.userService = userService;
        this.postRepo = postRepo;
        this.postMapper = postMapper;
        this.commentRepo = commentRepo;
        this.reactionRepo = reactionRepo;
    }

    private PostResponse toResponse(Post post) {
        long commentCount = commentRepo.countByPostId(post.getId());
        long reactionCount = reactionRepo.countByPostId(post.getId());
        return postMapper.toResponse(post, commentCount, reactionCount);
    }

    public PostResponse createPost(PostRequest postRequest, UUID userId) {
        User user = userService.getById(userId);
        Post post = new Post();
        post.setUser(user);
        post.setTitle(postRequest.title());
        post.setContent(postRequest.content());
        post.setTags(postRequest.techStack());
        post.setVisibility(postRequest.visibility());
        Post saved = postRepo.save(post);
        return toResponse(saved);
    }

    public List<PostResponse> getPublicPosts() {
        return postRepo.findByVisibility(PostVisibility.PUBLIC)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<PostResponse> getPostsByUsername(String username) {
        PostFetchStrategy strategy =
                AuthUtil.isAuthenticated(username)
                        ? new LoggedInPostFetchStrategy(postRepo)
                        : new PublicPostFetchStrategy(postRepo);

        return strategy.fetchPosts(username)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public PostResponse updatePost(PostRequest req, String username, UUID postId) {
        Post post = getOwnedPost(username, postId);
        if(req.title() != null) post.setTitle(req.title());
        if(req.content() != null) post.setContent(req.content());
        if(req.techStack() != null) post.setTags(req.techStack());
        if(req.visibility() != null) post.setVisibility(req.visibility());
        Post updatedPost = postRepo.save(post);
        return toResponse(updatedPost);
    }

    public void deletePost(UUID postId, String username) {
        Post post = getOwnedPost(username, postId);
        postRepo.delete(post);
    }

    private Post getOwnedPost(String username, UUID postId) {
        return postRepo.findByIdAndUser_Username(postId, username)
                .orElseThrow(() -> new AccessDeniedException("Access Denied"));
    }

    public Post getById(UUID postId) {
        return postRepo.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
    }

    public boolean checkPrivatePost(UUID postId) {
        Post post = getById(postId);
        if (post.getVisibility() == PostVisibility.PRIVATE) {
            throw new AccessDeniedException("Access Denied");
        }
        return true;
    }


}
