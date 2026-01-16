package com.example.demo.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserProfileRequest(
        @NotBlank(message = "password must not be blank")
        String password,
        String skills,
        String bio,
        @NotBlank(message = "username must not be blank")
        String username,
        @NotBlank(message = "email must not be blank")
        @Email(message = "email format is invalid")
        String email
) {}
