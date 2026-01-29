package com.example.demo.service;

import com.example.demo.CommentService;
import com.example.demo.UserService;
import com.example.demo.dto.comment.CommentResponse;
import com.example.demo.entity.Comment;
import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import com.example.demo.enums.PostVisibility;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.CommentRepository;
import com.example.demo.security.AuthUtil;
import com.example.demo.post.PostService;
import com.example.demo.reaction.ReactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CommentServiceTest {

    @Mock private UserService userService;
    @Mock private PostService postService;
    @Mock private CommentRepository commentRepo;
    @Mock private ReactionService reactionService;
    @Mock private AuthUtil authUtil;

    @InjectMocks
    private CommentService commentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAddComment_PublicPost_Success() {
        UUID postId = UUID.randomUUID();
        String username = "user1";
        String content = "Hello comment";

        User user = new User();
        user.setUsername(username);

        Post post = new Post();
        post.setId(postId);
        post.setVisibility(PostVisibility.PUBLIC);

        Comment savedComment = new Comment();
        savedComment.setId(UUID.randomUUID());
        savedComment.setContent(content);
        savedComment.setUser(user);
        savedComment.setPost(post);

        when(userService.getByUsername(username)).thenReturn(user);
        when(postService.getById(postId)).thenReturn(post);
        when(commentRepo.save(any(Comment.class))).thenReturn(savedComment);
        when(reactionService.getCountByCommentId(savedComment.getId())).thenReturn(0L);

        CommentResponse response = commentService.addComment(postId, content, username);

        assertNotNull(response);
        assertEquals(savedComment.getId(), response.id());
        assertEquals(content, response.content());
        assertEquals(username, response.username());
        assertEquals(0L, response.reactionsCount());

        verify(commentRepo).save(any(Comment.class));
    }

    @Test
    void testAddComment_PrivatePost_ShouldThrowAccessDenied() {
        UUID postId = UUID.randomUUID();
        String username = "user1";
        String content = "Hello";

        User user = new User();
        Post post = new Post();
        post.setVisibility(PostVisibility.PRIVATE);

        when(userService.getByUsername(username)).thenReturn(user);
        when(postService.getById(postId)).thenReturn(post);

        assertThrows(AccessDeniedException.class, () ->
                commentService.addComment(postId, content, username)
        );

        verify(commentRepo, never()).save(any());
    }

    @Test
    void testDeleteComment_Success() {
        UUID commentId = UUID.randomUUID();
        Comment comment = new Comment();
        User user = new User();
        user.setUsername("user1");
        comment.setUser(user);

        when(commentRepo.findById(commentId)).thenReturn(Optional.of(comment));
        doNothing().when(authUtil).verifyUserAccess("user1");

        commentService.deleteComment(commentId);

        verify(commentRepo).delete(comment);
        verify(authUtil).verifyUserAccess("user1");
    }

    @Test
    void testDeleteComment_NotFound_ShouldThrowResourceNotFound() {
        UUID commentId = UUID.randomUUID();
        when(commentRepo.findById(commentId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                commentService.deleteComment(commentId)
        );

        verify(commentRepo, never()).delete(any());
    }

    @Test
    void testGetCommentByPostId_Success() {
        UUID postId = UUID.randomUUID();

        // Mock Post
        Post post = new Post();
        post.setId(postId);

        // Mock Comments with Users
        User user1 = new User();
        user1.setUsername("user1");
        Comment comment1 = new Comment();
        comment1.setId(UUID.randomUUID());
        comment1.setUser(user1);
        comment1.setContent("First comment");

        User user2 = new User();
        user2.setUsername("user2");
        Comment comment2 = new Comment();
        comment2.setId(UUID.randomUUID());
        comment2.setUser(user2);
        comment2.setContent("Second comment");

        // Mock behaviors
        when(postService.getById(postId)).thenReturn(post);
        doNothing().when(postService).checkPrivatePost(postId);
        when(commentRepo.findAllByPostId(postId)).thenReturn(List.of(comment1, comment2));
        when(reactionService.getCountByCommentId(comment1.getId())).thenReturn(2L);
        when(reactionService.getCountByCommentId(comment2.getId())).thenReturn(5L);

        // Call the method
        List<CommentResponse> responses = commentService.getCommentByPostId(postId);

        // Assertions
        assertEquals(2, responses.size());

        CommentResponse response1 = responses.get(0);
        assertEquals(comment1.getId(), response1.id());
        assertEquals("First comment", response1.content());
        assertEquals("user1", response1.username());
        assertEquals(2L, response1.reactionsCount());

        CommentResponse response2 = responses.get(1);
        assertEquals(comment2.getId(), response2.id());
        assertEquals("Second comment", response2.content());
        assertEquals("user2", response2.username());
        assertEquals(5L, response2.reactionsCount());

        // Verify repository call
        verify(commentRepo).findAllByPostId(postId);
    }


    @Test
    void testGetCountByPostId() {
        UUID postId = UUID.randomUUID();
        when(commentRepo.countByPostId(postId)).thenReturn(5L);

        long count = commentService.getCountByPostId(postId);

        assertEquals(5L, count);
        verify(commentRepo).countByPostId(postId);
    }

    @Test
    void testGetById_Found() {
        UUID commentId = UUID.randomUUID();
        Comment comment = new Comment();
        when(commentRepo.findById(commentId)).thenReturn(Optional.of(comment));

        Comment found = commentService.getById(commentId);

        assertEquals(comment, found);
    }

    @Test
    void testGetById_NotFound() {
        UUID commentId = UUID.randomUUID();
        when(commentRepo.findById(commentId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> commentService.getById(commentId));
    }
}
