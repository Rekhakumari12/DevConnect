package com.example.demo.service;

import com.example.demo.dto.PostRequest;
import com.example.demo.dto.PostResponse;
import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import com.example.demo.enums.PostVisibility;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PostService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private PostRepository projectIdeaRepo;

    @Autowired
    private AuthUtil authUtil;

    private PostResponse toResponse(Post post) {
        PostResponse res = new PostResponse();
        res.id = post.getId();
        res.title = post.getTitle();
        res.content = post.getContent();
        res.techStack = post.getTechStack();
        res.visibility = post.getVisibility().name();
        res.createdBy = post.getUser().getUsername();
        res.createdAt = post.getCreatedAt();
        res.updatedAt = post.getUpdatedAt();
        return res;
    }

    public PostResponse createPost(PostRequest postRequest, String username) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(()-> new RuntimeException("User not found!"));
        Post post = new Post();
        post.setUser(user);
        post.setTitle(postRequest.title);
        post.setContent(postRequest.content);
        post.setTechStack(postRequest.techStack);
        post.setVisibility(postRequest.visibility);

        Post saved = projectIdeaRepo.save(post);
        return toResponse(saved);
    }

    public List<PostResponse> getPublicPosts() {
        List<Post> posts = projectIdeaRepo.findByVisibility(PostVisibility.PUBLIC);
        return posts.stream().map(this::toResponse).toList();
    }

    public List<PostResponse> getPostsByUsername(String username) {
        boolean sameUser = authUtil.isSameUser(username);
        List<Post> posts = projectIdeaRepo.findAllByUser_Username(username);
        if (!sameUser) {
            return posts.stream().filter(post -> post.getVisibility() == PostVisibility.PUBLIC).map(this::toResponse).toList();
        }
        return posts.stream().map(this::toResponse).toList();
    }

    public PostResponse updatePost(PostRequest req, String username, UUID postId) {
        Post post = getOwnedPost(username, postId);

        if(req.title!=null) post.setTitle(req.title);
        if(req.content!=null) post.setContent(req.content);
        if(req.techStack!=null) post.setTechStack(req.techStack);
        if(req.visibility!=null) post.setVisibility(req.visibility);

        Post updatedPost = projectIdeaRepo.save(post);

        return toResponse(updatedPost);

    }

    public void deletePost(UUID postId, String username) {
        Post post = getOwnedPost(username, postId);
        projectIdeaRepo.delete(post);
    }

    // private helper method
    private Post getOwnedPost(String username, UUID postId) {
        return projectIdeaRepo.findByIdAndUser_Username(postId, username)
                .orElseThrow(() -> new AccessDeniedException("Post not found or not owned by you!"));
    }

}
