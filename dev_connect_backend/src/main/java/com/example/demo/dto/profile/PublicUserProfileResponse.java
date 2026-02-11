package com.example.demo.dto.profile;

import com.example.demo.entity.User;

import java.util.List;
import java.util.UUID;

public record PublicUserProfileResponse(
        UUID id,
        String username,
        String email,
        List<String> skills,
        String bio
) {
    public PublicUserProfileResponse(User user) {
        this(
                user.getId(),
                user.getUsername(),
                user.isShowEmailPublicly() ? user.getEmail() : null,
                user.getSkills(),
                user.getBio()
        );
    }
}
