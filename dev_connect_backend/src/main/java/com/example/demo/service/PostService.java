package com.example.demo.service;

import com.example.demo.dto.PostRequest;
import com.example.demo.dto.PostResponse;
import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import com.example.demo.enums.PostVisibility;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.AuthUtil;
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

    private PostResponse toResponse(Post post) {
        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getTags(),
                post.getVisibility().name(),
                post.getUser().getUsername(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                commentService.getCommentByPostId(post.getId()),
                reactionMapper.toReactionMap(post.getReactions())
        );
    }


    public PostResponse createPost(PostRequest postRequest, UUID userId) {
        User user = userRepo.findById(userId);
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
        List<Post> posts = postRepo.findByVisibility(PostVisibility.PUBLIC);
        return postRepo.findByVisibility(PostVisibility.PUBLIC)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<PostResponse> getPostsByUserId(UUID userId) {
        boolean sameUser = authUtil.isSameUser(userId);
        List<Post> posts = postRepo.findAllByUser_Id(userId);
        if (!sameUser) {
            return posts.stream().filter(post -> post.getVisibility() == PostVisibility.PUBLIC).map(this::toResponse).toList();
        }
        return posts.stream().map(this::toResponse).toList();
    }

    public PostResponse updatePost(PostRequest req, UUID userId, UUID postId) {
        Post post = getOwnedPost(userId, postId);
        if(req.title() != null) post.setTitle(req.title());
        if(req.content() != null) post.setContent(req.content());
        if(req.techStack() != null) post.setTags(req.techStack());
        if(req.visibility() != null) post.setVisibility(req.visibility());
        Post updatedPost = postRepo.save(post);
        return toResponse(updatedPost);
    }

    public void deletePost(UUID postId, UUID userId) {
        Post post = getOwnedPost(userId, postId);
        postRepo.delete(post);
    }

    private Post getOwnedPost(UUID userId, UUID postId) {
        return postRepo.findByIdAndUser_Id(postId, userId)
                .orElseThrow(() -> new AccessDeniedException("Post not found or not owned by you!"));
    }

    public Page<PostResponse> searchPosts(String keyword, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(
                page == null ? 0 : page,
                size == null ? 10 : size
        );

        Page<Post> posts = postRepo.searchPublicPosts(keyword, pageable);
        return  posts.map(this::toResponse);
    }

}
