package com.example.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// Represents a row in your users table
@Entity

@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @NotBlank
    public String password;

    public String skills;
    public String bio;

    @NotBlank
    @Column(unique = true)
    public String username;

    @NotBlank
    @Email
    @Column(unique = true)
    public String email;
}
