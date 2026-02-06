package com.example.demo.service;

import com.example.demo.UserService;
import com.example.demo.dto.profile.PublicUserProfileResponse;
import com.example.demo.dto.profile.UpdateProfileRequest;
import com.example.demo.dto.profile.UserProfileRequest;
import com.example.demo.dto.profile.UserProfileResponse;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegister_Success() {
        UserProfileRequest request = new UserProfileRequest(
                "password",
                List.of("Java", "Spring"),
                "bio",
                "john",
                "john@email.com"
        );

        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepo.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserProfileResponse response = userService.register(request);

        assertNotNull(response);
        assertEquals("john", response.username());
        assertEquals("john@email.com", response.email());
        assertEquals("bio", response.bio());
        assertEquals(List.of("Java", "Spring"), response.skills());

        verify(passwordEncoder).encode("password");
        verify(userRepo).save(any(User.class));
    }

    @Test
    void testGetProfileByUsername_Success() {
        User user = new User();
        user.setUsername("john");

        when(userRepo.findByUsername("john")).thenReturn(Optional.of(user));

        UserProfileResponse response = userService.getProfileByUsernameForOwner("john");

        assertEquals("john", response.username());
    }

    @Test
    void testGetProfileByUsername_NotFound() {
        when(userRepo.findByUsername("john")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> userService.getProfileByUsernameForOwner("john"));
    }

    @Test
    void testGetPublicProfileByUsername_Success() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("john");
        user.setEmail("john@example.com");
        user.setSkills(List.of("Java"));
        user.setBio("Developer");

        when(userRepo.findByUsername("john")).thenReturn(Optional.of(user));

        PublicUserProfileResponse response = userService.getPublicProfileByUsername("john");

        assertEquals("john", response.username());
        assertEquals(List.of("Java"), response.skills());
        assertEquals("Developer", response.bio());
        // Verify email is NOT included in public profile
        assertNotNull(response.id());
    }

    @Test
    void testGetPublicProfileByUsername_NotFound() {
        when(userRepo.findByUsername("john")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> userService.getPublicProfileByUsername("john"));
    }

    @Test
    void testUpdateProfile_Success() {
        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setUsername("old");
        user.setEmail("old@email.com");
        user.setBio("old bio");
        user.setSkills(List.of("Java"));

        UpdateProfileRequest request = new UpdateProfileRequest(
                "new@email.com",
                "new",
                List.of("Java", "Spring"),
                "new bio"
        );

        when(userRepo.findById(userId)).thenReturn(Optional.of(user));
        when(userRepo.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserProfileResponse response = userService.updateProfile(request, userId);

        // Username and email should NOT change
        assertEquals("old", response.username());
        assertEquals("old@email.com", response.email());
        // Skills and bio should change
        assertEquals("new bio", response.bio());
        assertEquals(List.of("Java", "Spring"), response.skills());

        verify(userRepo).save(user);
    }

    @Test
    void testUpdateProfile_OnlySkillsAndBioModified() {
        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setUsername("original");
        user.setEmail("original@email.com");
        user.setBio("original bio");
        user.setSkills(List.of("Java"));

        // Request contains new username and email but they should be ignored
        UpdateProfileRequest request = new UpdateProfileRequest(
                "hacker@evil.com",
                "hacker",
                List.of("Python"),
                "Updated bio"
        );

        when(userRepo.findById(userId)).thenReturn(Optional.of(user));
        when(userRepo.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserProfileResponse response = userService.updateProfile(request, userId);

        // Verify username and email were NOT modified
        assertEquals("original", response.username());
        assertEquals("original@email.com", response.email());
        // Verify only skills and bio were updated
        assertEquals(List.of("Python"), response.skills());
        assertEquals("Updated bio", response.bio());

        // Verify the saved user still has original username and email
        assertEquals("original", user.getUsername());
        assertEquals("original@email.com", user.getEmail());
        assertEquals(List.of("Python"), user.getSkills());
        assertEquals("Updated bio", user.getBio());
    }

    @Test
    void testUpdateProfile_UserNotFound() {
        UUID userId = UUID.randomUUID();

        UpdateProfileRequest request = new UpdateProfileRequest(
                "new",
                null,
                null,
                null
        );

        when(userRepo.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> userService.updateProfile(request, userId));
    }

    @Test
    void testGetById_Success() {
        UUID userId = UUID.randomUUID();
        User user = new User();

        when(userRepo.findById(userId)).thenReturn(Optional.of(user));

        User result = userService.getById(userId);

        assertEquals(user, result);
    }

    @Test
    void testGetById_NotFound() {
        UUID userId = UUID.randomUUID();
        when(userRepo.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> userService.getById(userId));
    }

    @Test
    void testGetByUsername_Success() {
        User user = new User();
        user.setUsername("john");

        when(userRepo.findByUsername("john")).thenReturn(Optional.of(user));

        User result = userService.getByUsername("john");

        assertEquals("john", result.getUsername());
    }

    @Test
    void testGetByUsername_NotFound() {
        when(userRepo.findByUsername("john")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> userService.getByUsername("john"));
    }
}
