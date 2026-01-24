package com.example.demo;

import com.example.demo.controller.UserController;
import com.example.demo.dto.profile.UserProfileRequest;
import com.example.demo.dto.profile.UserProfileResponse;
import com.example.demo.entity.User;
import com.example.demo.exception.GlobalExceptionHandler;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtFilter;
import com.example.demo.security.JwtUtil;
import com.example.demo.service.UserService;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(UserController.class)
@Import(GlobalExceptionHandler.class)
class UserControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepo;

    @MockitoBean
    private UserService userService;


    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private JwtFilter jwtFilter;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Test
    void registerReturnsProfile() throws Exception {
        UUID uuid = UUID.randomUUID();
        User user = new User();
        user.setId(uuid);
        user.setUsername("rekha");
        user.setEmail("rekha@example.com");
        user.setPassword("encodedpass");
        user.setSkills("Java, Spring");
        user.setBio("Software Developer");

        UserProfileResponse profile = new UserProfileResponse(user);
        when(userService.register(any(UserProfileRequest.class))).thenReturn(profile);

        String json = """
            {
                "username": "rekha",
                "email": "rekha@example.com",
                "password": "encodedpass",
                "skills": "Java, Spring",
                "bio": "Software Developer"
            }
        """;

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }

    @Test
    void registerFailsWithInvalidJson() throws Exception {
        String json = """
            { "username": "rekha"
        """; // broken JSON

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerFailsWhenMissingFields() throws Exception {
        String json = """
            {
                "email": "abc@example.com"
            }
        """;

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerFailsWhenFieldsAreBlank() throws Exception {
        String json = """
            {
                "username": "",
                "password": "",
                "email": ""
            }
        """;

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void passwordIsEncodedBeforeSaving() throws Exception {
        when(passwordEncoder.encode("rawpass")).thenReturn("ENCODED_VALUE");
        when(userRepo.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            return u;
        });

        String json = """
            {
                "username": "rekha",
                "password": "rawpass",
                "email": "rekha@example.com"
            }
        """;

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }

    @Test
    void registerRejectsInvalidEmailFormat() throws Exception {
        String json = """
            {
                "username": "rekha",
                "password": "rawpass",
                "email": "not-an-email"
            }
        """;

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

}
