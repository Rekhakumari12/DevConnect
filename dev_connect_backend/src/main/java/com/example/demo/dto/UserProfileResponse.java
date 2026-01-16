package com.example.demo.dto;

import com.example.demo.entity.User;

import java.util.UUID;

public record UserProfileResponse(
        UUID id,
        String username,
        String email,
        String skills,
        String bio
) {
    public UserProfileResponse(User user) {
        this(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getSkills(),
                user.getBio()
        );
    }
}
