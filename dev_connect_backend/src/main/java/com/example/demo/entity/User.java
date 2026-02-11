package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String password;
    private List<String> skills;
    private String bio;
    @Column(unique = true)
    private String username;
    @Column(unique = true)
    private String email;
    
    @Column(nullable = false)
    private boolean showEmailPublicly = false;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {   // Hibernate uses this after insert
        this.id = id;
    }

    public boolean isShowEmailPublicly() {
        return showEmailPublicly;
    }

    public void setShowEmailPublicly(boolean showEmailPublicly) {
        this.showEmailPublicly = showEmailPublicly;
    }
}
