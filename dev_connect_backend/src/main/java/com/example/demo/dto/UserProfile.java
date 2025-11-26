package com.example.demo.dto;

import com.example.demo.entity.User;

public class UserProfile {
    public String username;
    public String email;
    public String skills;
    public String bio;

    public UserProfile(User user) {
        this.username = user.username;
        this.email = user.email;
        this.skills = user.skills;
        this.bio = user.bio;
    }
}
