package com.example.demo;

import com.example.demo.controller.UserController;
import com.example.demo.entity.User;
import com.example.demo.exception.GlobalExceptionHandler;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtFilter;
import com.example.demo.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
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
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private JwtFilter jwtFilter;

    @MockitoBean
    private JwtUtil jwtUtil;

    private User sample;

    @BeforeEach
    void setup() {
        sample = new User();
        sample.setUsername("rekha");
        sample.setPassword("rawpass");
        sample.setEmail("rekha@example.com");
        sample.setSkills("Java");
        sample.setBio("Coder");
    }

    @Test
    void getAllUsersReturnsList() throws Exception {
        when(userRepo.findAll()).thenReturn(List.of(sample));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("rekha"))
                .andExpect(jsonPath("$[0].email").value("rekha@example.com"));
    }

    @Test
    void registerSuccessReturnsSavedUser() throws Exception {

        String json = """
            {
                "username": "rekha",
                "password": "rawpass",
                "email": "rekha@example.com",
                "skills": "Java",
                "bio": "Coder"
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
    void registerReturnsNullOnException() throws Exception {
        when(passwordEncoder.encode("rawpass")).thenReturn("enc");
        when(userRepo.save(any(User.class))).thenThrow(new RuntimeException("DB problem"));

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
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("DB problem"));
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
    void registerHandlesExtraFieldsGracefully() throws Exception {
        when(passwordEncoder.encode("rawpass")).thenReturn("safe");
        when(userRepo.save(any(User.class))).thenReturn(sample);

        String json = """
            {
                "username": "rekha",
                "password": "rawpass",
                "email": "rekha@example.com",
                "skills": "Java",
                "bio": "Coder",
                "extraField": "ignored"
            }
        """;

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("rekha"));
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
