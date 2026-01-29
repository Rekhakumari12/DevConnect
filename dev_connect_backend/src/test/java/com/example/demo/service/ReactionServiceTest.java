package com.example.demo.service;

import com.example.demo.UserService;
import com.example.demo.dto.reaction.ReactionResponse;
import com.example.demo.dto.reaction.ReactionSummary;
import com.example.demo.entity.Comment;
import com.example.demo.entity.Post;
import com.example.demo.entity.Reaction;
import com.example.demo.entity.User;
import com.example.demo.enums.ReactionType;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.reaction.ReactionService;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.ReactionRepository;
import com.example.demo.post.PostService;
import com.example.demo.utils.ReactionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReactionServiceTest {

    @Mock private ReactionRepository reactionRepo;
    @Mock private ReactionMapper reactionMapper;
    @Mock private CommentRepository commentRepo;
    @Mock private UserService userService;
    @Mock private PostService postService;

    @InjectMocks
    private ReactionService reactionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetReactionsByPostId_Success() {
        UUID postId = UUID.randomUUID();
        Post post = new Post();

        List<Reaction> reactions = List.of(new Reaction(), new Reaction());
        List<ReactionSummary> summaries = List.of(mock(ReactionSummary.class));

        when(postService.getById(postId)).thenReturn(post);
        doNothing().when(postService).checkPrivatePost(postId);
        when(reactionRepo.findAllByPostId(postId)).thenReturn(reactions);
        when(reactionMapper.toReactionMap(reactions)).thenReturn(summaries);

        List<ReactionSummary> result = reactionService.getReactionsByPostId(postId);

        assertEquals(summaries, result);
        verify(reactionRepo).findAllByPostId(postId);
    }

    @Test
    void testGetReactionsByCommentId_Success() {
        UUID commentId = UUID.randomUUID();
        Comment comment = new Comment();

        List<Reaction> reactions = List.of(new Reaction());
        List<ReactionSummary> summaries = List.of(mock(ReactionSummary.class));

        when(commentRepo.findById(commentId)).thenReturn(Optional.of(comment));
        when(reactionRepo.findAllByCommentId(commentId)).thenReturn(reactions);
        when(reactionMapper.toReactionMap(reactions)).thenReturn(summaries);

        List<ReactionSummary> result = reactionService.getReactionsByCommentId(commentId);

        assertEquals(summaries, result);
    }

    @Test
    void testGetReactionsByCommentId_NotFound() {
        UUID commentId = UUID.randomUUID();
        when(commentRepo.findById(commentId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> reactionService.getReactionsByCommentId(commentId));
    }

    @Test
    void testReactToPost_NewReaction() {
        UUID postId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Post post = new Post();
        User user = new User();
        user.setId(userId);

        when(postService.getById(postId)).thenReturn(post);
        doNothing().when(postService).checkPrivatePost(postId);
        when(userService.getById(userId)).thenReturn(user);
        when(reactionRepo.findByUserIdAndPostId(userId, postId))
                .thenReturn(Optional.empty());
        when(reactionRepo.save(any(Reaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ReactionResponse response =
                reactionService.reactToPost(postId, ReactionType.LIKE, userId);

        assertEquals(ReactionType.LIKE, response.type());
        assertEquals(userId, response.userId());
    }

    @Test
    void testReactToPost_UndoReaction() {
        UUID postId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Post post = new Post();
        User user = new User();
        user.setId(userId);

        Reaction existing = new Reaction();
        existing.setType(ReactionType.LIKE);

        when(postService.getById(postId)).thenReturn(post);
        doNothing().when(postService).checkPrivatePost(postId);
        when(userService.getById(userId)).thenReturn(user);
        when(reactionRepo.findByUserIdAndPostId(userId, postId))
                .thenReturn(Optional.of(existing));

        ReactionResponse response =
                reactionService.reactToPost(postId, ReactionType.LIKE, userId);

        assertNull(response.type());
        verify(reactionRepo).delete(existing);
    }

    @Test
    void testReactToComment_Success() {
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Comment comment = new Comment();
        User user = new User();
        user.setId(userId);

        when(commentRepo.findById(commentId)).thenReturn(Optional.of(comment));
        when(userService.getById(userId)).thenReturn(user);
        when(reactionRepo.findByUserIdAndCommentId(userId, commentId))
                .thenReturn(Optional.empty());
        when(reactionRepo.save(any(Reaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ReactionResponse response =
                reactionService.reactToComment(commentId, ReactionType.LIKE, userId);

        assertEquals(ReactionType.LIKE, response.type());
        assertEquals(userId, response.userId());
    }

    @Test
    void testReactToComment_CommentNotFound() {
        UUID commentId = UUID.randomUUID();
        when(commentRepo.findById(commentId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> reactionService.reactToComment(commentId, ReactionType.LIKE, UUID.randomUUID()));
    }

    @Test
    void testGetCountByPostId() {
        UUID postId = UUID.randomUUID();
        when(reactionRepo.countByPostId(postId)).thenReturn(10L);

        long count = reactionService.getCountByPostId(postId);

        assertEquals(10L, count);
    }

    @Test
    void testGetCountByCommentId() {
        UUID commentId = UUID.randomUUID();
        when(reactionRepo.countByCommentId(commentId)).thenReturn(3L);

        long count = reactionService.getCountByCommentId(commentId);

        assertEquals(3L, count);
    }
}
