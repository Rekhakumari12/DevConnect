package com.example.demo.service;

import com.example.demo.dto.PostResponse;
import com.example.demo.entity.Post;
import com.example.demo.repository.PostRepository;
import com.example.demo.utils.PostMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
public class SearchService {

    @Autowired
    private PostRepository postRepo;

    @Autowired
    private PostMapper postMapper;

    public Page<PostResponse> searchPosts(String keyword, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(
                page == null ? 0 : page,
                size == null ? 10 : size
        );

        Page<Post> posts = postRepo.searchPublicPosts(keyword, pageable);
        return  posts.map(post -> postMapper.toResponse(post));
    }
}
