package com.example.demo.dto;


import jakarta.validation.constraints.NotBlank;

public record UpdateProfileRequest (
    @NotBlank
    String email,
    @NotBlank
    String username,
    String skills,
    String bio
){}
