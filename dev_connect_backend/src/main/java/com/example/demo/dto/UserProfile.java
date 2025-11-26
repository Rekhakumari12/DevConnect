package com.example.demo.dto;

import com.example.demo.entity.User;

import java.util.UUID;

public class UserProfile {
    public UUID id;
    public String username;
    public String email;
    public String skills;
    public String bio;

    public UserProfile(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.skills = user.getSkills();
        this.bio = user.getBio();
    }
}
