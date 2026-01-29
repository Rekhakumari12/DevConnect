package com.example.demo.service;

import com.example.demo.UserService;
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

        UserProfileResponse response = userService.getProfileByUsername("john");

        assertEquals("john", response.username());
    }

    @Test
    void testGetProfileByUsername_NotFound() {
        when(userRepo.findByUsername("john")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> userService.getProfileByUsername("john"));
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

        assertEquals("new", response.username());
        assertEquals("new@email.com", response.email());
        assertEquals("new bio", response.bio());
        assertEquals(List.of("Java", "Spring"), response.skills());

        verify(userRepo).save(user);
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
