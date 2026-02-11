package com.example.demo.dto.profile;

import com.example.demo.entity.User;

import java.util.List;
import java.util.UUID;

public record UserProfileResponse(
        UUID id,
        String username,
        String email,
        List<String> skills,
        String bio,
        boolean showEmailPublicly
) {
    public UserProfileResponse(User user) {
        this(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getSkills(),
                user.getBio(),
                user.isShowEmailPublicly()
        );
    }
}
