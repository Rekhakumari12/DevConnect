//package com.example.demo;
//
//import com.example.demo.dto.UpdateProfileRequest;
//import com.example.demo.dto.UserProfile;
//import com.example.demo.entity.User;
//import com.example.demo.repository.UserRepository;
//
//import com.example.demo.security.JwtFilter;
//import com.example.demo.security.JwtUtil;
//import com.example.demo.service.UserService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.ArgumentCaptor;
//import org.mockito.InjectMocks;
//import org.mockito.MockitoAnnotations;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//
//import java.security.Principal;
//import java.util.Optional;
//import java.util.UUID;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//
//public class UserServiceTests {
//
//    @MockitoBean
//    private UserRepository userRepo;
//
//    @MockitoBean
//    private PasswordEncoder passwordEncoder;
//
//    @MockitoBean
//    private Principal principal;
//
//    @InjectMocks
//    private UserService userService;
//
//    @BeforeEach
//    void setup() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    @Test
//    void registerShouldEncodePasswordAndSaveUser() {
//        User user = new User();
//        user.setPassword("raw_pass");
//
//        when(passwordEncoder.encode("raw_pass")).thenReturn("encoded_pass");
//
//        User savedUser = new User();
//        savedUser.setPassword("encoded_pass");
//
//        when(userRepo.save(any(User.class))).thenReturn(savedUser);
//
//        UserProfile profile = userService.register(user);
//
//        assertEquals("encoded_pass", user.getPassword());
//        assertNotNull(profile);
//        verify(passwordEncoder).encode("raw_pass");
//        verify(userRepo).save(user);
//    }
//
//    @Test
//    void getProfileShouldReturnProfileIfUserExists() {
//        when(principal.getName()).thenReturn("john");
//        User user = new User();
//        user.setUsername("john");
//
//        when(userRepo.findByUsername("john")).thenReturn(Optional.of(user));
//
//        UserProfile profile = userService.getProfile(principal);
//
//        assertEquals("john", profile.username);
//        verify(userRepo).findByUsername("john");
//    }
//
//    @Test
//    void getProfileShouldThrowIfUserMissing() {
//        when(principal.getName()).thenReturn("missing");
//        when(userRepo.findByUsername("missing")).thenReturn(Optional.empty());
//
//        RuntimeException ex =
//                assertThrows(RuntimeException.class, () -> userService.getProfile(principal));
//
//        assertEquals("User not found", ex.getMessage());
//    }
//
//    @Test
//    void updateProfileShouldModifyFieldsAndSave() {
//        UpdateProfileRequest req = new UpdateProfileRequest();
//        req.email = "new@email.com";
//        req.username = "newUser";
//        req.skills = "Java,Spring";
//        req.bio = "Updated bio";
//
//        when(principal.getName()).thenReturn("john");
//
//        User user = new User();
//        user.setUsername("john");
//
//        when(userRepo.findByUsername("john")).thenReturn(Optional.of(user));
//        when(userRepo.save(any(User.class))).thenReturn(user);
//
//        UserProfile updated = userService.updateProfile(req, principal);
//
//        assertEquals("newUser", user.getUsername());
//        assertEquals("new@email.com", user.getEmail());
//        assertEquals("Java,Spring", user.getSkills());
//        assertEquals("Updated bio", user.getBio());
//
//        assertNotNull(updated);
//        verify(userRepo).save(user);
//    }
//
//    @Test
//    void updateProfileShouldThrowIfUserNotFound() {
//        when(principal.getName()).thenReturn("ghost");
//        when(userRepo.findByUsername("ghost")).thenReturn(Optional.empty());
//
//        RuntimeException ex =
//                assertThrows(RuntimeException.class, () -> userService.updateProfile(new UpdateProfileRequest(), principal));
//
//        assertEquals("User not found", ex.getMessage());
//    }
//
//    @Test
//    void updateProfileShouldOnlyUpdateProvidedFields() {
//        UpdateProfileRequest req = new UpdateProfileRequest();
//        req.email = null;
//        req.username = "changed";
//        req.skills = null;
//        req.bio = "bio changed";
//
//        when(principal.getName()).thenReturn("john");
//
//        User user = new User();
//        user.setEmail("old@email.com");
//        user.setUsername("john");
//        user.setSkills("old skills");
//        user.setBio("old bio");
//
//        when(userRepo.findByUsername("john")).thenReturn(Optional.of(user));
//        when(userRepo.save(any(User.class))).thenReturn(user);
//
//        userService.updateProfile(req, principal);
//
//        assertEquals("changed", user.getUsername());
//        assertEquals("old@email.com", user.getEmail());
//        assertEquals("old skills", user.getSkills());
//        assertEquals("bio changed", user.getBio());
//    }
//}
