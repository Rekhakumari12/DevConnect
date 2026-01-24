package com.example.demo.dto.profile;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record UserProfileRequest(
        @NotBlank(message = "password must not be blank")
        String password,
        List<String> skills,
        String bio,
        @NotBlank(message = "username must not be blank")
        String username,
        @NotBlank(message = "email must not be blank")
        @Email(message = "email format is invalid")
        String email
) {}
