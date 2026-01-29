package com.example.demo.service;

import com.example.demo.CommentService;
import com.example.demo.SearchService;
import com.example.demo.dto.post.PostResponse;
import com.example.demo.entity.Post;
import com.example.demo.repository.PostRepository;
import com.example.demo.reaction.ReactionService;
import com.example.demo.utils.PostMapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @Mock
    private PostRepository postRepo;

    @Mock
    private PostMapper postMapper;

    @Mock
    private CommentService commentService;

    @Mock
    private ReactionService reactionService;

    @InjectMocks
    private SearchService searchService;

    @Test
    void testSearchPosts() {
        String keyword = "test";

        Post post1 = new Post();
        post1.setId(UUID.randomUUID());

        Post post2 = new Post();
        post2.setId(UUID.randomUUID());

        Pageable pageable = PageRequest.of(0, 10);
        Page<Post> postPage = new PageImpl<>(List.of(post1, post2), pageable, 2);

        PostResponse resp1 = new PostResponse(
                post1.getId(), "T1", "C1", List.of(),
                "PUBLIC", "U1",
                LocalDateTime.now(), LocalDateTime.now(), 1, 2
        );

        PostResponse resp2 = new PostResponse(
                post2.getId(), "T2", "C2", List.of(),
                "PUBLIC", "U2",
                LocalDateTime.now(), LocalDateTime.now(), 3, 4
        );

        when(postRepo.searchPublicPosts(eq(keyword), any(Pageable.class)))
                .thenReturn(postPage);

        when(commentService.getCountByPostId(post1.getId())).thenReturn(1L);
        when(reactionService.getCountByPostId(post1.getId())).thenReturn(2L);
        when(commentService.getCountByPostId(post2.getId())).thenReturn(3L);
        when(reactionService.getCountByPostId(post2.getId())).thenReturn(4L);

        when(postMapper.toResponse(post1, 1, 2)).thenReturn(resp1);
        when(postMapper.toResponse(post2, 3, 4)).thenReturn(resp2);

        Page<PostResponse> result = searchService.searchPosts(keyword, 0, 10);

        assertEquals(2, result.getTotalElements());
        assertEquals(resp1, result.getContent().get(0));
        assertEquals(resp2, result.getContent().get(1));
    }
}
