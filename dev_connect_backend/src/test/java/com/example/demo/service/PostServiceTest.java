package com.example.demo.service;

import com.example.demo.UserService;
import com.example.demo.dto.post.PostRequest;
import com.example.demo.dto.post.PostResponse;
import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import com.example.demo.enums.PostVisibility;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.post.PostService;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.ReactionRepository;
import com.example.demo.utils.PostMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PostServiceTest {

    @Mock private UserService userService;
    @Mock private PostRepository postRepo;
    @Mock private PostMapper postMapper;
    @Mock private CommentRepository commentRepo;
    @Mock private ReactionRepository reactionRepo;

    @InjectMocks
    private PostService postService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreatePost_Success() {
        UUID userId = UUID.randomUUID();
        PostRequest request = new PostRequest("Title", "Content", List.of("Java"), PostVisibility.PUBLIC);
        User user = new User();
        user.setId(userId);

        Post savedPost = new Post();
        savedPost.setId(UUID.randomUUID());
        savedPost.setUser(user);
        savedPost.setTitle("Title");
        savedPost.setContent("Content");
        savedPost.setTags(List.of("Java"));
        savedPost.setVisibility(PostVisibility.PUBLIC);

        PostResponse response = new PostResponse(savedPost.getId(), "Title", "Content", List.of("Java"), "PUBLIC", "user", LocalDateTime.now(),LocalDateTime.now(),0,0);

        when(userService.getById(userId)).thenReturn(user);
        when(postRepo.save(any(Post.class))).thenReturn(savedPost);
        when(commentRepo.countByPostId(savedPost.getId())).thenReturn(0L);
        when(reactionRepo.countByPostId(savedPost.getId())).thenReturn(0L);
        when(postMapper.toResponse(savedPost,0,0)).thenReturn(response);

        PostResponse result = postService.createPost(request, userId);

        assertNotNull(result);
        assertEquals(savedPost.getId(), result.id());
        assertEquals("Title", result.title());
        verify(postRepo).save(any(Post.class));
    }

    @Test
    void testGetPublicPosts() {
        Post post1 = new Post();
        post1.setId(UUID.randomUUID());
        Post post2 = new Post();
        post2.setId(UUID.randomUUID());

        PostResponse resp1 = new PostResponse(
                post1.getId(), "T1", "C1", List.of(),
                "PUBLIC", "U1",
                LocalDateTime.now(), LocalDateTime.now(), 0, 0
        );

        PostResponse resp2 = new PostResponse(
                post2.getId(), "T2", "C2", List.of(),
                "PUBLIC", "U2",
                LocalDateTime.now(), LocalDateTime.now(), 0, 0
        );

        Pageable pageable = PageRequest.of(0, 10);
        Page<Post> postPage = new PageImpl<>(List.of(post1, post2), pageable, 2);

        when(postRepo.findByVisibility(eq(PostVisibility.PUBLIC), any(Pageable.class)))
                .thenReturn(postPage);

        when(commentRepo.countByPostId(any())).thenReturn(0L);
        when(reactionRepo.countByPostId(any())).thenReturn(0L);
        when(postMapper.toResponse(post1, 0, 0)).thenReturn(resp1);
        when(postMapper.toResponse(post2, 0, 0)).thenReturn(resp2);

        Page<PostResponse> results = postService.getPublicPosts(0, 10);

        assertEquals(2, results.getContent().size());
        assertEquals(resp1, results.getContent().get(0));
        assertEquals(resp2, results.getContent().get(1));
    }


    @Test
    void testGetById_Found() {
        UUID postId = UUID.randomUUID();
        Post post = new Post();
        when(postRepo.findById(postId)).thenReturn(Optional.of(post));

        Post result = postService.getById(postId);

        assertEquals(post, result);
    }

    @Test
    void testGetById_NotFound() {
        UUID postId = UUID.randomUUID();
        when(postRepo.findById(postId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> postService.getById(postId));
    }

    @Test
    void testUpdatePost_Success() {
        UUID postId = UUID.randomUUID();
        String username = "user1";

        PostRequest req = new PostRequest("New Title", "New Content", List.of("Spring"), PostVisibility.PUBLIC);

        Post existingPost = new Post();
        existingPost.setId(postId);
        User user = new User();
        user.setUsername(username);
        existingPost.setUser(user);

        Post updatedPost = new Post();
        updatedPost.setId(postId);
        updatedPost.setTitle(req.title());
        updatedPost.setContent(req.content());
        updatedPost.setTags(req.techStack());
        updatedPost.setVisibility(req.visibility());
        updatedPost.setUser(user);

        PostResponse response = new PostResponse(postId, req.title(), req.content(), req.techStack(), "PUBLIC" ,"user1", LocalDateTime.now(), LocalDateTime.now(), 0, 0);

        when(postRepo.findByIdAndUser_Username(postId, username)).thenReturn(Optional.of(existingPost));
        when(postRepo.save(existingPost)).thenReturn(updatedPost);
        when(commentRepo.countByPostId(postId)).thenReturn(0L);
        when(reactionRepo.countByPostId(postId)).thenReturn(0L);
        when(postMapper.toResponse(updatedPost,0,0)).thenReturn(response);

        PostResponse result = postService.updatePost(req, username, postId);

        assertEquals(response, result);
        verify(postRepo).save(existingPost);
    }

    @Test
    void testDeletePost_Success() {
        UUID postId = UUID.randomUUID();
        String username = "user1";

        Post post = new Post();
        post.setId(postId);
        User user = new User();
        user.setUsername(username);
        post.setUser(user);

        when(postRepo.findByIdAndUser_Username(postId, username)).thenReturn(Optional.of(post));

        postService.deletePost(postId, username);

        verify(postRepo).delete(post);
    }

    @Test
    void testDeletePost_AccessDenied() {
        UUID postId = UUID.randomUUID();
        String username = "user1";

        when(postRepo.findByIdAndUser_Username(postId, username)).thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class, () -> postService.deletePost(postId, username));
        verify(postRepo, never()).delete(any());
    }

    @Test
    void testCheckPrivatePost_AccessDenied() {
        UUID postId = UUID.randomUUID();
        Post post = new Post();
        post.setVisibility(PostVisibility.PRIVATE);

        when(postRepo.findById(postId)).thenReturn(Optional.of(post));

        assertThrows(AccessDeniedException.class, () -> postService.checkPrivatePost(postId));
    }

    @Test
    void testCheckPrivatePost_Public() {
        UUID postId = UUID.randomUUID();
        Post post = new Post();
        post.setVisibility(PostVisibility.PUBLIC);

        when(postRepo.findById(postId)).thenReturn(Optional.of(post));

        assertDoesNotThrow(() -> postService.checkPrivatePost(postId));
    }
}
