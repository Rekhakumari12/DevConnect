package com.example.demo.service;

import com.example.demo.dto.post.PostResponse;
import com.example.demo.entity.Post;
import com.example.demo.repository.PostRepository;
import com.example.demo.service.reaction.ReactionService;
import com.example.demo.utils.PostMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class SearchService {

    private final PostRepository postRepo;
    private final PostMapper postMapper;
    private final CommentService commentService;
    private final ReactionService reactionService;

    @Autowired
    public SearchService(
            PostRepository postRepo,
            PostMapper postMapper,
            CommentService commentService,
            ReactionService reactionService
    ) {
        this.postRepo = postRepo;
        this.postMapper = postMapper;
        this.commentService = commentService;
        this.reactionService = reactionService;
    }

    private PostResponse toResponse(Post post) {
        long commentCount = commentService.getCountByPostId(post.getId());
        long reactionCount = reactionService.getCountByPostId(post.getId());
        return postMapper.toResponse(post, commentCount, reactionCount);
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
